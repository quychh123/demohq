package com.shopcore.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.shopcore.sync.ShopSyncManager
import com.shopcore.sync.ShopSyncUiState

@Composable
fun RenderAuthGateScreen(
  appTitle: String,
  appSubtitle: String,
  @DrawableRes heroImageRes: Int,
  syncState: ShopSyncUiState,
  syncManager: ShopSyncManager,
  snackbarHostState: SnackbarHostState,
  modifier: Modifier = Modifier,
) {
  var serverUrlInput by remember(syncState.session.serverUrl) {
    mutableStateOf(syncState.session.serverUrl)
  }
  var isRegisterMode by remember { mutableStateOf(false) }
  var emailInput by remember { mutableStateOf("") }
  var passwordInput by remember { mutableStateOf("") }
  var shopNameInput by remember { mutableStateOf("HQ Apple & Kho Hàng Min") }
  var showPassword by remember { mutableStateOf(false) }

  Scaffold(
    modifier = modifier.fillMaxSize(),
    snackbarHost = { SnackbarHost(snackbarHostState) },
    contentWindowInsets = WindowInsets.systemBars,
  ) { innerPadding ->
    Box(
      modifier = Modifier.fillMaxSize().padding(innerPadding),
      contentAlignment = Alignment.TopCenter,
    ) {
      Column(
        modifier =
          Modifier.widthIn(max = 520.dp)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
      ) {
        Card(
          shape = MaterialTheme.shapes.large,
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
        ) {
          Box(modifier = Modifier.fillMaxWidth().height(160.dp)) {
            Image(
              painter = painterResource(id = heroImageRes),
              contentDescription = appTitle,
              contentScale = ContentScale.Crop,
              modifier = Modifier.fillMaxSize(),
            )
            Box(
              modifier =
                Modifier.fillMaxSize()
                  .background(
                    Brush.verticalGradient(
                      listOf(Color(0x550F172A), Color(0xE60F172A))
                    )
                  )
                  .padding(18.dp)
            ) {
              Column(
                modifier = Modifier.align(Alignment.BottomStart),
                verticalArrangement = Arrangement.spacedBy(6.dp),
              ) {
                Surface(
                  color = MaterialTheme.colorScheme.primary,
                  shape = RoundedCornerShape(99.dp),
                ) {
                  Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                  ) {
                    Icon(
                      imageVector = Icons.Default.CloudDone,
                      contentDescription = null,
                      tint = Color.White,
                      modifier = Modifier.size(14.dp),
                    )
                    Text(
                      text = "ĐỒNG BỘ SERVER RENDER",
                      style = MaterialTheme.typography.labelSmall,
                      color = Color.White,
                      fontWeight = FontWeight.Bold,
                    )
                  }
                }
                Text(
                  text = appTitle,
                  style = MaterialTheme.typography.headlineMedium,
                  color = Color.White,
                )
                Text(
                  text = appSubtitle,
                  style = MaterialTheme.typography.bodyMedium,
                  color = Color(0xFFE2E8F0),
                )
              }
            }
          }
        }

        if (syncState.isBusy) {
          LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        Card(
          shape = MaterialTheme.shapes.large,
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.55f)),
        ) {
          Column(
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              FilterChip(
                selected = !isRegisterMode,
                onClick = { isRegisterMode = false },
                label = { Text("Đăng nhập") },
                modifier = Modifier.testTag("auth_gate_login_tab"),
              )
              FilterChip(
                selected = isRegisterMode,
                onClick = { isRegisterMode = true },
                label = { Text("Đăng ký") },
                modifier = Modifier.testTag("auth_gate_register_tab"),
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
                modifier = Modifier.weight(1f).testTag("auth_gate_server_url"),
              )
              OutlinedButton(
                onClick = { syncManager.pingServer(serverUrlInput) },
                enabled = !syncState.isBusy,
                modifier = Modifier.testTag("auth_gate_ping_btn"),
              ) {
                Text("Kiểm tra")
              }
            }

            if (isRegisterMode) {
              OutlinedTextField(
                value = shopNameInput,
                onValueChange = { shopNameInput = it },
                label = { Text("Tên cửa hàng / Chi nhánh") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("auth_gate_shop_name"),
              )
            }

            OutlinedTextField(
              value = emailInput,
              onValueChange = { emailInput = it },
              label = { Text("Email tài khoản") },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
              singleLine = true,
              modifier = Modifier.fillMaxWidth().testTag("auth_gate_email"),
            )

            OutlinedTextField(
              value = passwordInput,
              onValueChange = { passwordInput = it },
              label = { Text("Mật khẩu (tối thiểu 6 ký tự)") },
              singleLine = true,
              visualTransformation =
                if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
              trailingIcon = {
                IconButton(
                  onClick = { showPassword = !showPassword },
                  modifier = Modifier.size(48.dp),
                ) {
                  Icon(
                    imageVector =
                      if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    contentDescription = "Hiện hoặc ẩn mật khẩu",
                  )
                }
              },
              modifier = Modifier.fillMaxWidth().testTag("auth_gate_password"),
            )

            Button(
              onClick = {
                if (isRegisterMode) {
                  syncManager.register(serverUrlInput, emailInput, passwordInput, shopNameInput)
                } else {
                  syncManager.login(serverUrlInput, emailInput, passwordInput)
                }
              },
              enabled = !syncState.isBusy,
              modifier = Modifier.fillMaxWidth().height(50.dp).testTag("auth_gate_submit_btn"),
            ) {
              Icon(
                imageVector =
                  if (isRegisterMode) Icons.Default.PersonAdd else Icons.AutoMirrored.Filled.Login,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
              )
              Spacer(Modifier.size(8.dp))
              Text(if (isRegisterMode) "Đăng ký & Vào ứng dụng" else "Đăng nhập & Vào ứng dụng")
            }

            val note = syncState.serverStatusNote
            if (!note.isNullOrBlank()) {
              Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.small,
              ) {
                Text(
                  text = note,
                  style = MaterialTheme.typography.bodyMedium,
                  modifier = Modifier.fillMaxWidth().padding(10.dp),
                )
              }
            }
          }
        }

        TextButton(
          onClick = syncManager::enterOfflineMode,
          modifier = Modifier.align(Alignment.CenterHorizontally).testTag("auth_gate_offline_btn"),
        ) {
          Text("Vào dùng ngoại tuyến (chưa kết nối Render)")
        }
      }
    }
  }
}
