package com.khohangmin.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.khohangmin.ui.WarehouseUiState
import com.shopcore.sync.ShopSyncManager
import com.shopcore.sync.ShopSyncUiState
import com.shopcore.ui.SyncAndBackupPanel

@Composable
fun KhoSyncScreen(
  uiState: WarehouseUiState,
  syncState: ShopSyncUiState,
  syncManager: ShopSyncManager,
  productsCount: Int,
  customersCount: Int,
  repairsCount: Int,
  ordersCount: Int,
  cashbookCount: Int,
  modifier: Modifier = Modifier,
) {
  LazyColumn(
    modifier = modifier.fillMaxSize(),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp),
  ) {
    item(key = "sync_panel", contentType = "sync") {
      SyncAndBackupPanel(
        syncState = syncState,
        syncManager = syncManager,
        productsCount = productsCount,
        customersCount = customersCount,
        repairsCount = repairsCount,
        ordersCount = ordersCount,
        cashbookCount = cashbookCount,
      )
    }
  }
}
