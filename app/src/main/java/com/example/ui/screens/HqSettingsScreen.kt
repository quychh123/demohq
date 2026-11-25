package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.unit.dp
import com.example.ui.HqAppleUiState
import com.shopcore.sync.ShopSyncManager
import com.shopcore.sync.ShopSyncUiState
import com.shopcore.ui.SyncAndBackupPanel

@Composable
fun HqSettingsScreen(
  uiState: HqAppleUiState,
  syncState: ShopSyncUiState,
  syncManager: ShopSyncManager,
  onToggleDarkTheme: () -> Unit,
  onOpenKhoHangMin: () -> Unit,
  onClearAllData: () -> Unit,
  modifier: Modifier = Modifier,
) {
  var showClearConfirm by remember { mutableStateOf(false) }

  LazyColumn(
    modifier = modifier.fillMaxSize(),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp),
  ) {
    item(key = "hq_sync_backup_panel", contentType = "sync_panel") {
      SyncAndBackupPanel(
        syncState = syncState,
        syncManager = syncManager,
        productsCount = uiState.products.size,
        customersCount = uiState.customers.size,
        repairsCount = uiState.repairTickets.size,
        ordersCount = uiState.orders.size,
        cashbookCount = uiState.cashbookCount,
      )
    }

    item(key = "hq_local_bridge", contentType = "card") {
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
              imageVector = Icons.Default.Sync,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
            )
            Text(
              text = "Chuyển nhanh sang Kho Hàng Min",
              style = MaterialTheme.typography.titleMedium,
            )
          }
          Text(
            text =
              "Đang chia sẻ chung: ${uiState.products.size} mặt hàng · ${uiState.customers.size} khách hàng · ${uiState.repairTickets.size} phiếu sửa · ${uiState.orders.size} đơn hàng",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
          )
          Button(
            onClick = onOpenKhoHangMin,
            modifier = Modifier.fillMaxWidth().testTag("open_kho_hang_min_btn"),
          ) {
            Icon(Icons.Default.Inventory2, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.size(8.dp))
            Text("Mở ứng dụng Kho Hàng Min")
          }
        }
      }
    }

    item(key = "hq_theme_toggle", contentType = "card") {
      Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.55f)),
      ) {
        Row(
          modifier = Modifier.fillMaxWidth().padding(16.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            Icon(Icons.Default.DarkMode, contentDescription = null)
            Column {
              Text("Giao diện tối", style = MaterialTheme.typography.titleMedium)
              Text(
                "Bật nền tối cho quầy tiếp nhận",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          }
          Switch(
            checked = uiState.darkTheme,
            onCheckedChange = { onToggleDarkTheme() },
            modifier = Modifier.testTag("hq_dark_mode_switch"),
          )
        }
      }
    }

    item(key = "hq_clear_all_data", contentType = "button") {
      OutlinedButton(
        onClick = { showClearConfirm = true },
        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
        modifier = Modifier.fillMaxWidth().testTag("hq_clear_all_data_btn"),
      ) {
        Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.size(8.dp))
        Text("Xóa toàn bộ dữ liệu trên máy")
      }
    }
  }

  if (showClearConfirm) {
    AlertDialog(
      onDismissRequest = { showClearConfirm = false },
      title = { Text("Xóa toàn bộ dữ liệu trên máy?") },
      text = {
        Text(
          "Thao tác này sẽ làm trống hàng hóa, khách hàng, phiếu sửa, đơn hàng và sổ quỹ cục bộ của cả 2 ứng dụng. Dữ liệu đã sao lưu .hqbak hoặc trên server Render không bị ảnh hưởng."
        )
      },
      confirmButton = {
        Button(
          onClick = {
            showClearConfirm = false
            onClearAllData()
          },
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
          modifier = Modifier.testTag("confirm_hq_clear_all_data_btn"),
        ) {
          Text("Xóa sạch")
        }
      },
      dismissButton = {
        TextButton(onClick = { showClearConfirm = false }) { Text("Hủy") }
      },
    )
  }
}
