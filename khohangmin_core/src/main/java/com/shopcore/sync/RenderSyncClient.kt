package com.shopcore.sync

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.Immutable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

@Immutable
data class RenderSessionState(
  val serverUrl: String = DEFAULT_RENDER_URL,
  val authToken: String = "",
  val userEmail: String = "",
  val shopName: String = "",
  val lastSyncAtMillis: Long = 0L,
  val autoSyncEnabled: Boolean = false,
) {
  val isLoggedIn: Boolean
    get() = authToken.isNotBlank() && userEmail.isNotBlank()
}

private const val PREFS_NAME = "hq_render_sync_prefs"
private const val KEY_SERVER_URL = "server_url"
private const val KEY_AUTH_TOKEN = "auth_token"
private const val KEY_USER_EMAIL = "user_email"
private const val KEY_SHOP_NAME = "shop_name"
private const val KEY_LAST_SYNC = "last_sync_at"
private const val KEY_AUTO_SYNC = "auto_sync"
const val DEFAULT_RENDER_URL = "https://hqapple-khohangmin-sync.onrender.com"

class RenderSyncClient(context: Context) {

  private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
  private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

  private val client =
    OkHttpClient.Builder()
      .connectTimeout(15, TimeUnit.SECONDS)
      .readTimeout(25, TimeUnit.SECONDS)
      .writeTimeout(25, TimeUnit.SECONDS)
      .build()

  private val _session = MutableStateFlow(loadSession())
  val session: StateFlow<RenderSessionState> = _session.asStateFlow()

  private fun loadSession(): RenderSessionState =
    RenderSessionState(
      serverUrl = prefs.getString(KEY_SERVER_URL, DEFAULT_RENDER_URL) ?: DEFAULT_RENDER_URL,
      authToken = prefs.getString(KEY_AUTH_TOKEN, "") ?: "",
      userEmail = prefs.getString(KEY_USER_EMAIL, "") ?: "",
      shopName = prefs.getString(KEY_SHOP_NAME, "") ?: "",
      lastSyncAtMillis = prefs.getLong(KEY_LAST_SYNC, 0L),
      autoSyncEnabled = prefs.getBoolean(KEY_AUTO_SYNC, false),
    )

  fun normalizeServerUrl(rawUrl: String): Result<String> {
    val trimmed = rawUrl.trim().trimEnd('/')
    if (trimmed.isEmpty()) {
      return Result.failure(IllegalArgumentException("Vui lòng nhập URL server Render"))
    }
    val withScheme =
      if (trimmed.startsWith("https://", ignoreCase = true) ||
        trimmed.startsWith("http://", ignoreCase = true)
      ) {
        trimmed
      } else {
        "https://$trimmed"
      }
    val parsed = Uri.parse(withScheme)
    val scheme = parsed.scheme?.lowercase()
    val host = parsed.host
    if ((scheme != "https" && scheme != "http") || host.isNullOrBlank()) {
      return Result.failure(IllegalArgumentException("URL server Render không hợp lệ"))
    }
    return Result.success(withScheme)
  }

  fun updateServerUrl(rawUrl: String): Result<String> {
    return normalizeServerUrl(rawUrl).onSuccess { clean ->
      prefs.edit().putString(KEY_SERVER_URL, clean).apply()
      _session.value = _session.value.copy(serverUrl = clean)
    }
  }

  fun setAutoSyncEnabled(enabled: Boolean) {
    prefs.edit().putBoolean(KEY_AUTO_SYNC, enabled).apply()
    _session.value = _session.value.copy(autoSyncEnabled = enabled)
  }

  fun logout() {
    prefs.edit()
      .remove(KEY_AUTH_TOKEN)
      .remove(KEY_USER_EMAIL)
      .remove(KEY_SHOP_NAME)
      .apply()
    _session.value =
      _session.value.copy(
        authToken = "",
        userEmail = "",
        shopName = "",
      )
  }

  suspend fun pingHealth(rawUrl: String): Result<String> =
    withContext(Dispatchers.IO) {
      val cleanUrl = normalizeServerUrl(rawUrl).getOrElse { return@withContext Result.failure(it) }
      try {
        val req = Request.Builder().url("$cleanUrl/health").get().build()
        client.newCall(req).execute().use { res ->
          val bodyText = res.body?.string().orEmpty()
          if (!res.isSuccessful) {
            return@withContext Result.failure(
              IllegalStateException("Server phản hồi lỗi HTTP ${res.code}")
            )
          }
          val msg =
            try {
              JSONObject(bodyText).optString("service", "Server Render đang hoạt động")
            } catch (_: Exception) {
              "Server Render đang hoạt động"
            }
          prefs.edit().putString(KEY_SERVER_URL, cleanUrl).apply()
          _session.value = _session.value.copy(serverUrl = cleanUrl)
          Result.success(msg)
        }
      } catch (e: Exception) {
        Result.failure(
          IllegalStateException("Không kết nối được server Render: ${e.message ?: "Mất mạng"}")
        )
      }
    }

  suspend fun register(
    rawUrl: String,
    email: String,
    password: String,
    shopName: String,
  ): Result<RenderSessionState> =
    authenticate(
      rawUrl = rawUrl,
      endpoint = "/api/auth/register",
      body =
        JSONObject().apply {
          put("email", email.trim())
          put("password", password)
          put("shopName", shopName.trim().ifBlank { "HQ Apple & Kho Hàng Min" })
        },
    )

  suspend fun login(
    rawUrl: String,
    email: String,
    password: String,
  ): Result<RenderSessionState> =
    authenticate(
      rawUrl = rawUrl,
      endpoint = "/api/auth/login",
      body =
        JSONObject().apply {
          put("email", email.trim())
          put("password", password)
        },
    )

  private suspend fun authenticate(
    rawUrl: String,
    endpoint: String,
    body: JSONObject,
  ): Result<RenderSessionState> =
    withContext(Dispatchers.IO) {
      val cleanUrl = normalizeServerUrl(rawUrl).getOrElse { return@withContext Result.failure(it) }
      val email = body.optString("email", "")
      val password = body.optString("password", "")
      if (!email.contains("@") || !email.contains(".")) {
        return@withContext Result.failure(IllegalArgumentException("Email không đúng định dạng"))
      }
      if (password.length < 6) {
        return@withContext Result.failure(IllegalArgumentException("Mật khẩu phải từ 6 ký tự trở lên"))
      }

      try {
        val req =
          Request.Builder()
            .url("$cleanUrl$endpoint")
            .post(body.toString().toRequestBody(jsonMediaType))
            .build()
        client.newCall(req).execute().use { res ->
          val respText = res.body?.string().orEmpty()
          val json =
            try {
              JSONObject(respText)
            } catch (_: Exception) {
              JSONObject()
            }
          if (!res.isSuccessful) {
            val errMsg = json.optString("error", "Xác thực thất bại (HTTP ${res.code})")
            return@withContext Result.failure(IllegalStateException(errMsg))
          }
          val token = json.optString("token", "")
          val userObj = json.optJSONObject("user")
          val userEmail = userObj?.optString("email", email) ?: email
          val shopName = userObj?.optString("shopName", "HQ Apple & Kho Hàng Min") ?: "HQ Apple & Kho Hàng Min"
          require(token.isNotBlank()) { "Server không trả về token đăng nhập" }

          prefs.edit()
            .putString(KEY_SERVER_URL, cleanUrl)
            .putString(KEY_AUTH_TOKEN, token)
            .putString(KEY_USER_EMAIL, userEmail)
            .putString(KEY_SHOP_NAME, shopName)
            .apply()

          val nextState =
            _session.value.copy(
              serverUrl = cleanUrl,
              authToken = token,
              userEmail = userEmail,
              shopName = shopName,
            )
          _session.value = nextState
          Result.success(nextState)
        }
      } catch (e: Exception) {
        Result.failure(
          IllegalStateException("Lỗi kết nối server Render: ${e.message ?: "Không phản hồi"}")
        )
      }
    }

  suspend fun pushSnapshot(snapshotJson: String): Result<Long> =
    withContext(Dispatchers.IO) {
      val current = _session.value
      if (!current.isLoggedIn) {
        return@withContext Result.failure(IllegalStateException("Vui lòng đăng nhập server Render trước"))
      }
      try {
        val wrapper =
          JSONObject().apply {
            put("snapshot", JSONObject(snapshotJson))
          }
        val req =
          Request.Builder()
            .url("${current.serverUrl}/api/sync/push")
            .addHeader("Authorization", "Bearer ${current.authToken}")
            .post(wrapper.toString().toRequestBody(jsonMediaType))
            .build()

        client.newCall(req).execute().use { res ->
          val respText = res.body?.string().orEmpty()
          val json =
            try {
              JSONObject(respText)
            } catch (_: Exception) {
              JSONObject()
            }
          if (res.code == 401) {
            logout()
            return@withContext Result.failure(
              IllegalStateException("Phiên đăng nhập Render đã hết hạn, vui lòng đăng nhập lại")
            )
          }
          if (!res.isSuccessful) {
            return@withContext Result.failure(
              IllegalStateException(json.optString("error", "Đẩy dữ liệu lên Render thất bại (HTTP ${res.code})"))
            )
          }
          val syncedAt = json.optLong("syncedAtMillis", System.currentTimeMillis())
          prefs.edit().putLong(KEY_LAST_SYNC, syncedAt).apply()
          _session.value = _session.value.copy(lastSyncAtMillis = syncedAt)
          Result.success(syncedAt)
        }
      } catch (e: Exception) {
        Result.failure(IllegalStateException("Không thể đẩy dữ liệu lên Render: ${e.message ?: "Lỗi mạng"}"))
      }
    }

  suspend fun pullSnapshot(): Result<String?> =
    withContext(Dispatchers.IO) {
      val current = _session.value
      if (!current.isLoggedIn) {
        return@withContext Result.failure(IllegalStateException("Vui lòng đăng nhập server Render trước"))
      }
      try {
        val req =
          Request.Builder()
            .url("${current.serverUrl}/api/sync/pull")
            .addHeader("Authorization", "Bearer ${current.authToken}")
            .get()
            .build()

        client.newCall(req).execute().use { res ->
          val respText = res.body?.string().orEmpty()
          val json =
            try {
              JSONObject(respText)
            } catch (_: Exception) {
              JSONObject()
            }
          if (res.code == 401) {
            logout()
            return@withContext Result.failure(
              IllegalStateException("Phiên đăng nhập Render đã hết hạn, vui lòng đăng nhập lại")
            )
          }
          if (!res.isSuccessful) {
            return@withContext Result.failure(
              IllegalStateException(json.optString("error", "Tải dữ liệu từ Render thất bại (HTTP ${res.code})"))
            )
          }
          val snapshotObj = json.optJSONObject("snapshot")
          val syncedAt = System.currentTimeMillis()
          prefs.edit().putLong(KEY_LAST_SYNC, syncedAt).apply()
          _session.value = _session.value.copy(lastSyncAtMillis = syncedAt)
          Result.success(snapshotObj?.toString())
        }
      } catch (e: Exception) {
        Result.failure(IllegalStateException("Không thể tải dữ liệu từ Render: ${e.message ?: "Lỗi mạng"}"))
      }
    }
}
