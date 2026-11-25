package com.shopcore.sync

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.Immutable
import com.shopcore.backup.BackupSummary
import com.shopcore.backup.ShopBackupCodec
import com.shopcore.data.ShopRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Immutable
data class ShopSyncUiState(
  val session: RenderSessionState = RenderSessionState(),
  val offlineBypass: Boolean = false,
  val localQuickBackup: BackupSummary? = null,
  val pendingImportSummary: BackupSummary? = null,
  val pendingImportRawJson: String? = null,
  val isBusy: Boolean = false,
  val serverStatusNote: String? = null,
)

class ShopSyncManager(
  private val context: Context,
  private val repository: ShopRepository,
  private val clientAppName: String,
  private val scope: CoroutineScope,
  private val onNotify: (String) -> Unit,
) {
  private val renderClient = RenderSyncClient(context)

  private val offlineBypass = MutableStateFlow(false)
  private val localQuickBackup = MutableStateFlow<BackupSummary?>(null)
  private val pendingImport = MutableStateFlow<Pair<BackupSummary, String>?>(null)
  private val isBusy = MutableStateFlow(false)
  private val serverStatusNote = MutableStateFlow<String?>(null)

  private val metaFlow =
    combine(offlineBypass, localQuickBackup, pendingImport, isBusy, serverStatusNote) {
      bypass,
      localSnap,
      pending,
      busy,
      note ->
      MetaBundle(bypass, localSnap, pending, busy, note)
    }

  val uiState: StateFlow<ShopSyncUiState> =
    combine(renderClient.session, metaFlow) { session, meta ->
      ShopSyncUiState(
        session = session,
        offlineBypass = meta.offlineBypass,
        localQuickBackup = meta.localSnap,
        pendingImportSummary = meta.pending?.first,
        pendingImportRawJson = meta.pending?.second,
        isBusy = meta.isBusy,
        serverStatusNote = meta.statusNote,
      )
    }.stateIn(
      scope = scope,
      started = SharingStarted.Lazily,
      initialValue = ShopSyncUiState(),
    )

  init {
    scope.launch {
      localQuickBackup.value = repository.getLocalQuickBackupSummary(context)
    }
  }

  fun enterOfflineMode() {
    offlineBypass.value = true
  }

  fun openAuthScreen() {
    renderClient.logout()
    offlineBypass.value = false
    serverStatusNote.value = null
  }

  fun exportHqbakToUri(uri: Uri) {
    scope.launch {
      isBusy.value = true
      try {
        val payload = repository.exportHqbakJson(clientAppName)
        withContext(Dispatchers.IO) {
          context.contentResolver.openOutputStream(uri)?.use { out ->
            out.write(payload.toByteArray(Charsets.UTF_8))
          } ?: throw IllegalStateException("Không thể ghi vào tệp đã chọn")
        }
        onNotify("Đã xuất tệp sao lưu tổng .hqbak thành công")
      } catch (e: Exception) {
        onNotify("Lỗi xuất tệp .hqbak: ${e.message ?: "Không xác định"}")
      } finally {
        isBusy.value = false
      }
    }
  }

  fun inspectHqbakFromUri(uri: Uri) {
    scope.launch {
      isBusy.value = true
      try {
        val raw =
          withContext(Dispatchers.IO) {
            context.contentResolver.openInputStream(uri)?.use { input ->
              input.bufferedReader(Charsets.UTF_8).readText()
            } ?: throw IllegalStateException("Không thể đọc tệp đã chọn")
          }
        repository
          .inspectHqbakJson(raw)
          .onSuccess { summary -> pendingImport.value = summary to raw }
          .onFailure { err -> onNotify(err.message ?: "Tệp .hqbak không hợp lệ") }
      } catch (e: Exception) {
        onNotify("Lỗi đọc tệp .hqbak: ${e.message ?: "Không xác định"}")
      } finally {
        isBusy.value = false
      }
    }
  }

  fun dismissPendingImport() {
    pendingImport.value = null
  }

  fun confirmPendingImport() {
    val raw = pendingImport.value?.second ?: return
    scope.launch {
      isBusy.value = true
      repository
        .restoreFromHqbakJson(raw)
        .onSuccess { summary ->
          pendingImport.value = null
          onNotify(
            "Đã khôi phục tổng: ${summary.productsCount} SP, ${summary.customersCount} KH, ${summary.ordersCount} đơn"
          )
        }
        .onFailure { err -> onNotify(err.message ?: "Khôi phục .hqbak thất bại") }
      isBusy.value = false
    }
  }

  fun saveLocalQuickBackup() {
    scope.launch {
      isBusy.value = true
      repository
        .saveLocalQuickBackup(context, clientAppName)
        .onSuccess { summary ->
          localQuickBackup.value = summary
          onNotify("Đã tạo bản sao lưu tổng .hqbak trên máy")
        }
        .onFailure { err -> onNotify(err.message ?: "Không thể sao lưu trên máy") }
      isBusy.value = false
    }
  }

  fun restoreLocalQuickBackup() {
    scope.launch {
      isBusy.value = true
      repository
        .restoreLocalQuickBackup(context)
        .onSuccess { summary ->
          onNotify(
            "Đã khôi phục bản lưu máy (${summary.productsCount} SP · ${summary.ordersCount} đơn)"
          )
        }
        .onFailure { err -> onNotify(err.message ?: "Không thể khôi phục bản lưu máy") }
      isBusy.value = false
    }
  }

  fun pingServer(serverUrl: String) {
    scope.launch {
      isBusy.value = true
      renderClient
        .pingHealth(serverUrl)
        .onSuccess { note ->
          serverStatusNote.value = "Kết nối OK: $note"
          onNotify("Server Render sẵn sàng")
        }
        .onFailure { err ->
          serverStatusNote.value = err.message
          onNotify(err.message ?: "Không kết nối được server Render")
        }
      isBusy.value = false
    }
  }

  fun login(serverUrl: String, email: String, password: String) {
    scope.launch {
      isBusy.value = true
      renderClient
        .login(serverUrl, email, password)
        .onSuccess { session ->
          offlineBypass.value = false
          serverStatusNote.value = "Đã đăng nhập ${session.userEmail}"
          onNotify("Đăng nhập Render thành công")
        }
        .onFailure { err ->
          serverStatusNote.value = err.message
          onNotify(err.message ?: "Đăng nhập thất bại")
        }
      isBusy.value = false
    }
  }

  fun register(serverUrl: String, email: String, password: String, shopName: String) {
    scope.launch {
      isBusy.value = true
      renderClient
        .register(serverUrl, email, password, shopName)
        .onSuccess { session ->
          offlineBypass.value = false
          serverStatusNote.value = "Đã đăng ký & đăng nhập ${session.userEmail}"
          onNotify("Đăng ký tài khoản Render thành công")
        }
        .onFailure { err ->
          serverStatusNote.value = err.message
          onNotify(err.message ?: "Đăng ký thất bại")
        }
      isBusy.value = false
    }
  }

  fun logout() {
    renderClient.logout()
    offlineBypass.value = false
    serverStatusNote.value = "Đã đăng xuất khỏi server Render"
    onNotify("Đã đăng xuất Render")
  }

  fun setAutoSync(enabled: Boolean) {
    renderClient.setAutoSyncEnabled(enabled)
  }

  fun pushToRender() {
    scope.launch {
      isBusy.value = true
      val payload = repository.exportHqbakJson(clientAppName)
      renderClient
        .pushSnapshot(payload)
        .onSuccess {
          serverStatusNote.value = "Đã đẩy dữ liệu tổng 2 bản lên Render"
          onNotify("Đẩy đồng bộ lên Render thành công")
        }
        .onFailure { err ->
          serverStatusNote.value = err.message
          onNotify(err.message ?: "Đẩy lên Render thất bại")
        }
      isBusy.value = false
    }
  }

  fun pullFromRender() {
    scope.launch {
      isBusy.value = true
      renderClient
        .pullSnapshot()
        .onSuccess { remoteJson ->
          if (remoteJson == null) {
            onNotify("Tài khoản Render chưa có dữ liệu đồng bộ nào")
          } else {
            repository
              .restoreFromHqbakJson(remoteJson)
              .onSuccess { summary ->
                serverStatusNote.value =
                  "Đã tải từ Render: ${summary.productsCount} SP · ${summary.ordersCount} đơn"
                onNotify("Đã tải dữ liệu từ Render về máy")
              }
              .onFailure { err -> onNotify(err.message ?: "Dữ liệu Render không hợp lệ") }
          }
        }
        .onFailure { err ->
          serverStatusNote.value = err.message
          onNotify(err.message ?: "Tải từ Render thất bại")
        }
      isBusy.value = false
    }
  }

  fun syncBidirectionalWithRender() {
    scope.launch {
      isBusy.value = true
      renderClient
        .pullSnapshot()
        .onSuccess { remoteJson ->
          val finalPayload =
            if (remoteJson == null) {
              repository.exportHqbakJson(clientAppName)
            } else {
              val merged =
                repository
                  .mergeWithRemoteJson(remoteJson, clientAppName)
                  .getOrElse { err ->
                    onNotify(err.message ?: "Lỗi gộp dữ liệu đồng bộ")
                    isBusy.value = false
                    return@launch
                  }
              ShopBackupCodec.encode(merged)
            }

          renderClient
            .pushSnapshot(finalPayload)
            .onSuccess {
              serverStatusNote.value = "Đã đồng bộ 2 chiều Kho Hàng Min ↔ HQ Apple qua Render"
              onNotify("Đồng bộ 2 chiều Render hoàn tất")
            }
            .onFailure { err ->
              serverStatusNote.value = err.message
              onNotify(err.message ?: "Không thể đẩy bản gộp lên Render")
            }
        }
        .onFailure { err ->
          serverStatusNote.value = err.message
          onNotify(err.message ?: "Đồng bộ Render thất bại")
        }
      isBusy.value = false
    }
  }

  fun triggerAutoSyncIfEnabled() {
    val current = renderClient.session.value
    if (current.isLoggedIn && current.autoSyncEnabled && !isBusy.value) {
      pushToRender()
    }
  }

  private data class MetaBundle(
    val offlineBypass: Boolean,
    val localSnap: BackupSummary?,
    val pending: Pair<BackupSummary, String>?,
    val isBusy: Boolean,
    val statusNote: String?,
  )
}
