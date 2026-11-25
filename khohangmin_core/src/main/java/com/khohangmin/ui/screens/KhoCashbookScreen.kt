package com.khohangmin.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.khohangmin.ui.WarehouseUiState
import com.khohangmin.ui.theme.CrimsonLow
import com.khohangmin.ui.theme.CrimsonLowSoft
import com.khohangmin.ui.theme.ForestStock
import com.khohangmin.ui.theme.ForestStockSoft
import com.khohangmin.ui.theme.MonoMetricStyle
import com.khohangmin.ui.theme.MonoTagStyle
import com.shopcore.data.CashbookEntryEntity
import com.shopcore.ui.formatCompactVnd
import com.shopcore.ui.formatShortDateTime
import com.shopcore.ui.formatVnd

@Composable
fun KhoCashbookScreen(
  uiState: WarehouseUiState,
  onFilterChange: (String) -> Unit,
  onAddManualEntry: (
    isIncome: Boolean,
    title: String,
    amountVnd: Long,
    note: String,
    onSuccess: () -> Unit,
  ) -> Unit,
  modifier: Modifier = Modifier,
) {
  var showAddDialog by remember { mutableStateOf(false) }
  val filters = listOf("Tất cả", "Bán hàng", "Sửa chữa", "Thu khác", "Chi phí")

  Box(modifier = modifier.fillMaxSize()) {
    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 88.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      item(key = "cashbook_summary", contentType = "summary") {
        Card(
          shape = MaterialTheme.shapes.large,
        ) {
          Box(
            modifier =
              Modifier.fillMaxWidth()
                .background(
                  Brush.linearGradient(
                    listOf(Color(0xFF1E293B), Color(0xFF0F172A))
                  )
                )
                .padding(18.dp)
          ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
              Text(
                text = "SỔ QUỸ · GỐC VÀ LỜI",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF94A3B8),
                fontWeight = FontWeight.Bold,
              )
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
              ) {
                Column {
                  Text(
                    text = "Tiền lời ròng",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFCBD5E1),
                  )
                  Text(
                    text = formatVnd(uiState.totalProfitVnd),
                    style = MonoMetricStyle.copy(fontSize = MaterialTheme.typography.headlineMedium.fontSize),
                    color = if (uiState.totalProfitVnd >= 0) Color(0xFF4ADE80) else Color(0xFFFCA5A5),
                  )
                }
                Text(
                  text = "${uiState.cashbookEntries.size} bút toán",
                  style = MonoTagStyle,
                  color = Color(0xFF94A3B8),
                )
              }

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
              ) {
                SummaryBox(
                  label = "Tổng Thu (Bán + Sửa)",
                  amount = formatCompactVnd(uiState.totalRevenueVnd),
                  valueColor = Color.White,
                  modifier = Modifier.weight(1f),
                )
                SummaryBox(
                  label = "Tổng Gốc & Chi phí",
                  amount = formatCompactVnd(uiState.totalCostVnd),
                  valueColor = Color(0xFFFBBF24),
                  modifier = Modifier.weight(1f),
                )
              }
            }
          }
        }
      }

      item(key = "cashbook_filters", contentType = "filters") {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          items(filters, key = { it }) { filter ->
            FilterChip(
              selected = uiState.cashbookFilter == filter,
              onClick = { onFilterChange(filter) },
              label = { Text(filter) },
              modifier = Modifier.testTag("cashbook_filter_$filter"),
            )
          }
        }
      }

      if (uiState.filteredCashbook.isEmpty()) {
        item(key = "cashbook_empty", contentType = "empty") {
          Card(
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
          ) {
            Text(
              text = "Chưa có bút toán sổ quỹ nào. Bấm nút + bên dưới để ghi phiếu thu/chi hoặc bán hàng từ HQ Apple.",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.fillMaxWidth().padding(16.dp),
            )
          }
        }
      } else {
        items(uiState.filteredCashbook, key = { it.id }, contentType = { "entry" }) { entry ->
          CashbookEntryCard(entry = entry)
        }
      }
    }

    FloatingActionButton(
      onClick = { showAddDialog = true },
      containerColor = MaterialTheme.colorScheme.primary,
      contentColor = MaterialTheme.colorScheme.onPrimary,
      modifier =
        Modifier.align(Alignment.BottomEnd).padding(16.dp).testTag("add_cashbook_fab"),
    ) {
      Icon(Icons.Default.Add, contentDescription = "Thêm phiếu thu chi")
    }
  }

  if (showAddDialog) {
    AddCashbookEntryDialog(
      onDismiss = { showAddDialog = false },
      onConfirm = { isIncome, title, amount, note ->
        onAddManualEntry(isIncome, title, amount, note) {
          showAddDialog = false
        }
      },
    )
  }
}

@Composable
private fun SummaryBox(
  label: String,
  amount: String,
  valueColor: Color,
  modifier: Modifier = Modifier,
) {
  Surface(
    modifier = modifier,
    color = Color(0x22FFFFFF),
    shape = MaterialTheme.shapes.small,
  ) {
    Column(
      modifier = Modifier.padding(10.dp),
      verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
      Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = Color(0xFFCBD5E1),
      )
      Text(
        text = amount,
        style = MonoMetricStyle,
        color = valueColor,
      )
    }
  }
}

@Composable
private fun CashbookEntryCard(entry: CashbookEntryEntity) {
  val (typeLabel, badgeBg, badgeFg) =
    when (entry.entryType) {
      "SALE" -> Triple("Bán hàng", MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer)
      "REPAIR" -> Triple("Sửa chữa", Color(0xFFE0EEFF), Color(0xFF0066CC))
      "INCOME" -> Triple("Phiếu thu", ForestStockSoft, ForestStock)
      else -> Triple("Phiếu chi", CrimsonLowSoft, CrimsonLow)
    }

  Card(
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
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Surface(color = badgeBg, shape = RoundedCornerShape(6.dp)) {
            Text(
              text = typeLabel,
              style = MaterialTheme.typography.labelSmall,
              color = badgeFg,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            )
          }
          Text(
            text = entry.code,
            style = MonoTagStyle,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
        Text(
          text = formatShortDateTime(entry.createdAtMillis),
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }

      Text(
        text = entry.title,
        style = MaterialTheme.typography.titleMedium,
      )

      if (entry.note.isNotBlank()) {
        Text(
          text = entry.note,
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Column {
          Text(
            text = "Thu",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
          Text(
            text = formatVnd(entry.revenueVnd),
            style = MonoTagStyle,
          )
        }
        Column {
          Text(
            text = if (entry.entryType == "EXPENSE") "Tiền chi" else "Gốc",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
          Text(
            text = formatVnd(entry.costVnd),
            style = MonoTagStyle,
          )
        }
        Column(horizontalAlignment = Alignment.End) {
          Text(
            text = "Lời",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
          Text(
            text =
              if (entry.profitVnd >= 0) "+${formatVnd(entry.profitVnd)}"
              else formatVnd(entry.profitVnd),
            style = MonoMetricStyle,
            color = if (entry.profitVnd >= 0) ForestStock else CrimsonLow,
          )
        }
      }
    }
  }
}

@Composable
private fun AddCashbookEntryDialog(
  onDismiss: () -> Unit,
  onConfirm: (isIncome: Boolean, title: String, amountVnd: Long, note: String) -> Unit,
) {
  var isIncome by remember { mutableStateOf(false) }
  var title by remember { mutableStateOf("") }
  var amountText by remember { mutableStateOf("") }
  var note by remember { mutableStateOf("") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Tạo phiếu Sổ quỹ") },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          FilterChip(
            selected = !isIncome,
            onClick = { isIncome = false },
            label = { Text("Phiếu Chi (Vốn/Chi phí)") },
          )
          FilterChip(
            selected = isIncome,
            onClick = { isIncome = true },
            label = { Text("Phiếu Thu") },
          )
        }
        OutlinedTextField(
          value = title,
          onValueChange = { title = it },
          label = { Text("Nội dung *") },
          placeholder = { Text("VD: Nhập lô cường lực, Tiền điện...") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("cashbook_title_input"),
        )
        OutlinedTextField(
          value = amountText,
          onValueChange = { amountText = it.filter(Char::isDigit) },
          label = { Text("Số tiền (₫) *") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("cashbook_amount_input"),
        )
        OutlinedTextField(
          value = note,
          onValueChange = { note = it },
          label = { Text("Ghi chú") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          onConfirm(isIncome, title, amountText.toLongOrNull() ?: 0L, note)
        },
        modifier = Modifier.testTag("confirm_save_cashbook"),
      ) {
        Text("Lưu phiếu")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text("Hủy") }
    },
  )
}
