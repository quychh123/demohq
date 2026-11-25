package com.shopcore.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shopcore.sync.ShopSyncManager
import com.shopcore.sync.ShopSyncUiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SyncAndBackupPanel(
  syncState: ShopSyncUiState,
  syncManager: ShopSyncManager,
  productsCount: Int,
  customersCount: Int,
  repairsCount: Int,
  ordersCount: Int,
  cashbookCount: Int,
  modifier: Modifier = Modifier,
) {
  val exportLauncher =
    rememberLauncherForActivityResult(
      contract = ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri ->
      if (uri != null) {
        syncManager.exportHqbakToUri(uri)
      }
    }

  val importLauncher =
    rememberLauncherForActivityResult(
      contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
      if (uri != null) {
        syncManager.inspectHqbakFromUri(uri)
      }
    }

  var serverUrlInput by remember(syncState.session.serverUrl) {
    mutableStateOf(syncState.session.serverUrl)
  }

  Column(
    modifier = modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(14.dp),
  ) {
    if (syncState.isBusy) {
      LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
    }

    // 1. Backup & Restore dual-app (.hqbak)
    Card(
      shape = MaterialTheme.shapes.medium,
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.55f)),
    ) {
      Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          Icon(
            imageVector = Icons.Default.Backup,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
          )
          Text(
            text = "Sao lưu & Khôi phục tổng (.hqbak)",
            style = MaterialTheme.typography.titleMedium,
          )
        }

        Text(
          text =
            "Đóng gói toàn bộ dữ liệu cả 2 bản Kho Hàng Min & HQ Apple: $productsCount hàng hóa · $customersCount khách hàng · $repairsCount phiếu sửa · $ordersCount đơn hàng · $cashbookCount sổ quỹ.",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          Button(
            onClick = {
              val stamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.US).format(Date())
              exportLauncher.launch("hqapple_khohangmin_$stamp.hqbak")
            },
            enabled = !syncState.isBusy,
            modifier = Modifier.weight(1f).testTag("export_hqbak_btn"),
          ) {
            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.size(6.dp))
            Text("Xuất .hqbak")
          }

          OutlinedButton(
            onClick = { importLauncher.launch(arrayOf("*/*")) },
            enabled = !syncState.isBusy,
            modifier = Modifier.weight(1f).testTag("import_hqbak_btn"),
          ) {
            Icon(Icons.Default.FileOpen, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.size(6.dp))
            Text("Mở .hqbak")
          }
        }

        Surface(
          color = MaterialTheme.colorScheme.surfaceVariant,
          shape = MaterialTheme.shapes.small,
        ) {
          Column(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            val localSnap = syncState.localQuickBackup
            Text(
              text =
                if (localSnap != null) {
                  "Bản lưu nhanh trên máy (${formatShortDateTime(localSnap.exportedAtMillis)}): ${localSnap.productsCount} SP · ${localSnap.ordersCount} đơn · ${localSnap.cashbookCount} sổ quỹ"
                } else {
                  "Chưa có bản sao lưu nhanh trên máy"
                },
              style = MaterialTheme.typography.bodyMedium,
            )
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              OutlinedButton(
                onClick = syncManager::saveLocalQuickBackup,
                enabled = !syncState.isBusy,
                modifier = Modifier.weight(1f).testTag("save_local_hqbak_btn"),
              ) {
                Icon(Icons.Default.Backup, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.size(4.dp))
                Text("Lưu trên máy")
              }
              OutlinedButton(
                onClick = syncManager::restoreLocalQuickBackup,
                enabled = !syncState.isBusy && localSnap != null,
                modifier = Modifier.weight(1f).testTag("restore_local_hqbak_btn"),
              ) {
                Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.size(4.dp))
                Text("Khôi phục máy")
              }
            }
          }
        }
      }
    }

    // 2. Render Server Sync (without inline login/register form)
    Card(
      shape = MaterialTheme.shapes.medium,
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.55f)),
    ) {
      Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          Icon(
            imageVector = Icons.Default.CloudDone,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
          )
          Text(
            text = "Đồng bộ Server Render lẫn nhau",
            style = MaterialTheme.typography.titleMedium,
          )
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          OutlinedTextField(
            value = serverUrlInput,
            onValueChange = { serverUrlInput = it },
            label = { Text("URL Server Render") },
            placeholder = { Text("https://ten-server.onrender.com") },
            singleLine = true,
            modifier = Modifier.weight(1f).testTag("render_server_url_input"),
          )
          OutlinedButton(
            onClick = { syncManager.pingServer(serverUrlInput) },
            enabled = !syncState.isBusy,
            modifier = Modifier.testTag("render_ping_btn"),
          ) {
            Text("Kiểm tra")
          }
        }

        if (syncState.session.isLoggedIn) {
          Surface(
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f),
            shape = RoundedCornerShape(12.dp),
          ) {
            Row(
              modifier = Modifier.fillMaxWidth().padding(12.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
              ) {
                Text(
                  text = syncState.session.shopName.ifBlank { "Cửa hàng đồng bộ" },
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                )
                Text(
                  text = syncState.session.userEmail,
                  style = MaterialTheme.typography.bodyMedium,
                )
                val lastSync = syncState.session.lastSyncAtMillis
                Text(
                  text =
                    if (lastSync > 0L) "Đồng bộ gần nhất: ${formatShortDateTime(lastSync)}"
                    else "Chưa đồng bộ lần nào",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }
              OutlinedButton(
                onClick = syncManager::logout,
                enabled = !syncState.isBusy,
                modifier = Modifier.testTag("render_logout_btn"),
              ) {
                Icon(
                  Icons.AutoMirrored.Filled.Logout,
                  contentDescription = null,
                  modifier = Modifier.size(16.dp),
                )
                Spacer(Modifier.size(4.dp))
                Text("Đăng xuất")
              }
            }
          }

          Button(
            onClick = syncManager::syncBidirectionalWithRender,
            enabled = !syncState.isBusy,
            modifier = Modifier.fillMaxWidth().testTag("render_sync_both_btn"),
          ) {
            Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.size(8.dp))
            Text("Đồng bộ 2 chiều ngay (Kho ↔ HQ Apple)")
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            OutlinedButton(
              onClick = syncManager::pushToRender,
              enabled = !syncState.isBusy,
              modifier = Modifier.weight(1f).testTag("render_push_btn"),
            ) {
              Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(Modifier.size(4.dp))
              Text("Đẩy lên")
            }
            OutlinedButton(
              onClick = syncManager::pullFromRender,
              enabled = !syncState.isBusy,
              modifier = Modifier.weight(1f).testTag("render_pull_btn"),
            ) {
              Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(Modifier.size(4.dp))
              Text("Tải về")
            }
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Tự động đẩy lên Render khi có thay đổi",
                style = MaterialTheme.typography.bodyLarge,
              )
              Text(
                text = "Tự động cập nhật dữ liệu mỗi khi thêm đơn, sửa chữa hoặc nhập kho",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
            Switch(
              checked = syncState.session.autoSyncEnabled,
              onCheckedChange = syncManager::setAutoSync,
              modifier = Modifier.testTag("render_auto_sync_switch"),
            )
          }
        } else {
          Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(12.dp),
          ) {
            Column(
              modifier = Modifier.fillMaxWidth().padding(12.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              Text(
                text = "Đang sử dụng chế độ ngoại tuyến. Quay ra màn hình Đăng nhập ngoài ứng dụng để kết nối đồng bộ Server Render.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
              Button(
                onClick = syncManager::openAuthScreen,
                modifier = Modifier.fillMaxWidth().testTag("open_render_auth_btn"),
              ) {
                Icon(
                  Icons.AutoMirrored.Filled.Login,
                  contentDescription = null,
                  modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.size(8.dp))
                Text("Ra màn hình Đăng nhập / Đăng ký")
              }
            }
          }
        }

        val statusNote = syncState.serverStatusNote
        if (!statusNote.isNullOrBlank()) {
          Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = MaterialTheme.shapes.small,
          ) {
            Text(
              text = statusNote,
              style = MaterialTheme.typography.bodyMedium,
              modifier = Modifier.fillMaxWidth().padding(10.dp),
            )
          }
        }
      }
    }
  }

  val pendingSummary = syncState.pendingImportSummary
  if (pendingSummary != null) {
    AlertDialog(
      onDismissRequest = syncManager::dismissPendingImport,
      title = { Text("Xác nhận khôi phục tệp .hqbak") },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
          Text("Nguồn tạo: ${pendingSummary.sourceApp}")
          Text("Thời gian lưu: ${formatShortDateTime(pendingSummary.exportedAtMillis)}")
          Text("Hàng hóa: ${pendingSummary.productsCount} mã")
          Text("Khách hàng: ${pendingSummary.customersCount} khách")
          Text("Phiếu sửa chữa: ${pendingSummary.repairsCount} phiếu")
          Text("Đơn hàng: ${pendingSummary.ordersCount} đơn")
          Text("Sổ quỹ: ${pendingSummary.cashbookCount} bút toán")
          Text(
            text = "Lưu ý: Dữ liệu hiện tại của cả Kho Hàng Min và HQ Apple trên máy sẽ được thay thế bằng bản sao lưu này.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error,
          )
        }
      },
      confirmButton = {
        Button(
          onClick = syncManager::confirmPendingImport,
          modifier = Modifier.testTag("confirm_restore_hqbak_btn"),
        ) {
          Text("Khôi phục ngay")
        }
      },
      dismissButton = {
        TextButton(onClick = syncManager::dismissPendingImport) {
          Text("Hủy")
        }
      },
    )
  }
}
