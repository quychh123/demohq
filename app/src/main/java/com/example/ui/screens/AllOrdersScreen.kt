package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.HqAppleUiState
import com.example.ui.theme.EmeraldSoft
import com.example.ui.theme.EmeraldVerify
import com.example.ui.theme.MonoCodeStyle
import com.example.ui.theme.MonoPriceStyle
import com.example.ui.theme.WarmAmber
import com.example.ui.theme.WarmAmberSoft
import com.shopcore.data.ShopOrderEntity
import com.shopcore.ui.formatCompactVnd
import com.shopcore.ui.formatShortDateTime
import com.shopcore.ui.formatVnd

@Composable
fun AllOrdersScreen(
  uiState: HqAppleUiState,
  onSearchChange: (String) -> Unit,
  onTypeFilterChange: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  var selectedOrder by remember { mutableStateOf<ShopOrderEntity?>(null) }
  val filters = listOf("Tất cả", "Bán ra", "Sửa chữa")

  LazyColumn(
    modifier = modifier.fillMaxSize(),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    item(key = "orders_summary", contentType = "summary") {
      Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary),
      ) {
        Row(
          modifier = Modifier.fillMaxWidth().padding(16.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          Column {
            Text(
              text = "Tổng đơn",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.75f),
            )
            Text(
              text = "${uiState.orders.size} đơn",
              style = MonoPriceStyle,
              color = MaterialTheme.colorScheme.onSecondary,
            )
          }
          Column {
            Text(
              text = "Doanh thu hoàn tất",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.75f),
            )
            Text(
              text = formatCompactVnd(uiState.totalOrderRevenueVnd),
              style = MonoPriceStyle,
              color = MaterialTheme.colorScheme.onSecondary,
            )
          }
          Column(horizontalAlignment = Alignment.End) {
            Text(
              text = "Tiền lời",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.75f),
            )
            Text(
              text = "+${formatCompactVnd(uiState.totalOrderProfitVnd)}",
              style = MonoPriceStyle,
              color = EmeraldSoft,
            )
          }
        }
      }
    }

    item(key = "orders_search", contentType = "search") {
      OutlinedTextField(
        value = uiState.orderSearchQuery,
        onValueChange = onSearchChange,
        placeholder = { Text("Tìm mã đơn, tên khách, SĐT, sản phẩm/máy...") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        singleLine = true,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth().testTag("orders_search_input"),
      )
    }

    item(key = "orders_filters", contentType = "filters") {
      LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(filters, key = { it }) { filter ->
          FilterChip(
            selected = uiState.orderTypeFilter == filter,
            onClick = { onTypeFilterChange(filter) },
            label = { Text(filter) },
            modifier = Modifier.testTag("orders_filter_$filter"),
          )
        }
      }
    }

    if (uiState.filteredOrders.isEmpty()) {
      item(key = "orders_empty", contentType = "empty") {
        Card(
          shape = MaterialTheme.shapes.medium,
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
        ) {
          Text(
            text =
              if (uiState.orders.isEmpty())
                "Chưa có đơn hàng bán ra hoặc sửa chữa nào."
              else "Không tìm thấy đơn hàng phù hợp.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth().padding(16.dp),
          )
        }
      }
    } else {
      items(uiState.filteredOrders, key = { it.id }, contentType = { "order" }) { order ->
        UnifiedOrderCard(
          order = order,
          onClick = { selectedOrder = order },
        )
      }
    }
  }

  selectedOrder?.let { order ->
    AlertDialog(
      onDismissRequest = { selectedOrder = null },
      title = {
        Text(
          if (order.orderType == "REPAIR") "Chi tiết đơn sửa ${order.orderCode}"
          else "Chi tiết đơn bán ${order.orderCode}"
        )
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
          Text("Thời gian: ${formatShortDateTime(order.createdAtMillis)}")
          Text("Khách hàng: ${order.customerName} · ${order.customerPhone}")
          Text("Nội dung: ${order.summary}")
          Text("Bảo hành: ${order.warrantyType}")
          Text("Trạng thái: ${order.status} · ${order.paymentMethod}")
          Text("Tiền gốc: ${formatVnd(order.costTotalVnd)}", style = MonoCodeStyle)
          Text(
            text = "Tổng thu: ${formatVnd(order.totalVnd)}",
            style = MonoPriceStyle,
            color = MaterialTheme.colorScheme.primary,
          )
          Text(
            text = "Tiền lời: +${formatVnd(order.profitVnd)}",
            style = MonoPriceStyle,
            color = EmeraldVerify,
          )
        }
      },
      confirmButton = {
        Button(onClick = { selectedOrder = null }) { Text("Đóng") }
      },
    )
  }
}

@Composable
private fun UnifiedOrderCard(
  order: ShopOrderEntity,
  onClick: () -> Unit,
) {
  val isRepair = order.orderType == "REPAIR"
  val (statusBg, statusFg) =
    when (order.status) {
      "Hoàn tất" -> EmeraldSoft to EmeraldVerify
      "Đang sửa" -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.primary
      else -> WarmAmberSoft to WarmAmber
    }

  Card(
    modifier =
      Modifier.fillMaxWidth()
        .clip(MaterialTheme.shapes.medium)
        .clickable(onClick = onClick)
        .testTag("order_card_${order.id}"),
    shape = MaterialTheme.shapes.medium,
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.55f)),
  ) {
    Column(
      modifier = Modifier.fillMaxWidth().padding(14.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = RoundedCornerShape(6.dp),
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
              Icon(
                imageVector = if (isRepair) Icons.Default.Build else Icons.Default.ShoppingBag,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(13.dp),
              )
              Text(
                text = if (isRepair) "Sửa chữa" else "Bán ra",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Bold,
              )
            }
          }
          Text(
            text = order.orderCode,
            style = MonoCodeStyle,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
        Surface(color = statusBg, shape = RoundedCornerShape(99.dp)) {
          Text(
            text = order.status,
            style = MaterialTheme.typography.labelSmall,
            color = statusFg,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp),
          )
        }
      }

      Text(
        text = order.summary,
        style = MaterialTheme.typography.titleMedium,
      )

      Text(
        text = "${order.customerName} · ${order.customerPhone} · ${formatShortDateTime(order.createdAtMillis)}",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
          Icon(
            imageVector = Icons.Default.VerifiedUser,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(15.dp),
          )
          Text(
            text = order.warrantyType,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold,
          )
        }
        Column(horizontalAlignment = Alignment.End) {
          Text(
            text = formatVnd(order.totalVnd),
            style = MonoPriceStyle,
            color = MaterialTheme.colorScheme.onSurface,
          )
          Text(
            text = "Lời: +${formatVnd(order.profitVnd)}",
            style = MonoCodeStyle,
            color = EmeraldVerify,
          )
        }
      }
    }
  }
}
