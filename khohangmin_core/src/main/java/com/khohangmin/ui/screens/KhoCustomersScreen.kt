package com.khohangmin.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.khohangmin.ui.WarehouseUiState
import com.khohangmin.ui.theme.CrimsonLow
import com.khohangmin.ui.theme.ForestStock
import com.khohangmin.ui.theme.MonoMetricStyle
import com.shopcore.data.CustomerEntity
import com.shopcore.ui.formatVnd

@Composable
fun KhoCustomersScreen(
  uiState: WarehouseUiState,
  onSearchChange: (String) -> Unit,
  onSaveCustomer: (
    existingId: String?,
    name: String,
    phone: String,
    deviceNote: String,
    onSuccess: () -> Unit,
  ) -> Unit,
  onDeleteCustomer: (CustomerEntity) -> Unit,
  onToggleDarkTheme: () -> Unit,
  onOpenHqApple: () -> Unit,
  modifier: Modifier = Modifier,
) {
  var editingCustomer by remember { mutableStateOf<CustomerEntity?>(null) }
  var showFormDialog by remember { mutableStateOf(false) }

  Box(modifier = modifier.fillMaxSize()) {
    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 88.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      item(key = "cust_search", contentType = "search") {
        OutlinedTextField(
          value = uiState.customerSearchQuery,
          onValueChange = onSearchChange,
          placeholder = { Text("Tìm tên khách hàng, SĐT, dòng máy...") },
          leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
          singleLine = true,
          shape = MaterialTheme.shapes.medium,
          modifier = Modifier.fillMaxWidth().testTag("customer_search_input"),
        )
      }

      item(key = "cust_actions", contentType = "actions") {
        Card(
          shape = MaterialTheme.shapes.medium,
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
        ) {
          Column(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
              ) {
                Icon(Icons.Default.DarkMode, contentDescription = null)
                Text("Giao diện tối", style = MaterialTheme.typography.titleMedium)
              }
              Switch(
                checked = uiState.darkTheme,
                onCheckedChange = { onToggleDarkTheme() },
                modifier = Modifier.testTag("kho_dark_mode_switch"),
              )
            }
            OutlinedButton(
              onClick = onOpenHqApple,
              modifier = Modifier.fillMaxWidth().testTag("open_hq_apple_btn"),
            ) {
              Icon(Icons.Default.Storefront, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(Modifier.size(6.dp))
              Text("Mở HQ Apple")
            }
          }
        }
      }

      item(key = "cust_header", contentType = "header") {
        Text(
          text = "Danh sách khách hàng (${uiState.filteredCustomers.size})",
          style = MaterialTheme.typography.titleMedium,
        )
      }

      if (uiState.filteredCustomers.isEmpty()) {
        item(key = "cust_empty", contentType = "empty") {
          Card(
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
          ) {
            Text(
              text = "Chưa có khách hàng nào. Bấm nút + ở góc phải bên dưới để thêm khách hàng.",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.fillMaxWidth().padding(16.dp),
            )
          }
        }
      } else {
        items(uiState.filteredCustomers, key = { it.id }, contentType = { "customer" }) { customer ->
          CustomerCard(
            customer = customer,
            onEdit = {
              editingCustomer = customer
              showFormDialog = true
            },
            onDelete = { onDeleteCustomer(customer) },
          )
        }
      }
    }

    FloatingActionButton(
      onClick = {
        editingCustomer = null
        showFormDialog = true
      },
      containerColor = MaterialTheme.colorScheme.primary,
      contentColor = MaterialTheme.colorScheme.onPrimary,
      modifier =
        Modifier.align(Alignment.BottomEnd).padding(16.dp).testTag("add_customer_fab"),
    ) {
      Icon(Icons.Default.Add, contentDescription = "Thêm khách hàng")
    }
  }

  if (showFormDialog) {
    CustomerFormDialog(
      existing = editingCustomer,
      onDismiss = { showFormDialog = false },
      onConfirm = { name, phone, note ->
        onSaveCustomer(editingCustomer?.id, name, phone, note) {
          showFormDialog = false
        }
      },
    )
  }
}

@Composable
private fun CustomerCard(
  customer: CustomerEntity,
  onEdit: () -> Unit,
  onDelete: () -> Unit,
) {
  Card(
    shape = MaterialTheme.shapes.medium,
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.55f)),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(14.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Column(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.spacedBy(4.dp),
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          Icon(
            imageVector = Icons.Default.Person,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp),
          )
          Text(
            text = customer.name,
            style = MaterialTheme.typography.titleMedium,
          )
          Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = RoundedCornerShape(99.dp),
          ) {
            Text(
              text = "${customer.orderCount} đơn",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onPrimaryContainer,
              fontWeight = FontWeight.SemiBold,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            )
          }
        }
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          Icon(
            imageVector = Icons.Default.Phone,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(14.dp),
          )
          Text(
            text = customer.phone,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
        Text(
          text = customer.deviceNote,
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
          text = "Đã chi: ${formatVnd(customer.totalSpentVnd)}",
          style = MonoMetricStyle,
          color = ForestStock,
        )
      }

      Row {
        IconButton(
          onClick = onEdit,
          modifier = Modifier.size(48.dp).testTag("edit_customer_${customer.id}"),
        ) {
          Icon(Icons.Default.Edit, contentDescription = "Sửa khách hàng")
        }
        IconButton(
          onClick = onDelete,
          modifier = Modifier.size(48.dp).testTag("delete_customer_${customer.id}"),
        ) {
          Icon(Icons.Default.DeleteOutline, contentDescription = "Xóa khách hàng", tint = CrimsonLow)
        }
      }
    }
  }
}

@Composable
private fun CustomerFormDialog(
  existing: CustomerEntity?,
  onDismiss: () -> Unit,
  onConfirm: (name: String, phone: String, note: String) -> Unit,
) {
  var name by remember { mutableStateOf(existing?.name.orEmpty()) }
  var phone by remember { mutableStateOf(existing?.phone.orEmpty()) }
  var deviceNote by remember { mutableStateOf(existing?.deviceNote.orEmpty()) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(if (existing == null) "Thêm khách hàng" else "Cập nhật khách hàng") },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text("Họ tên khách hàng *") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("customer_name_input"),
        )
        OutlinedTextField(
          value = phone,
          onValueChange = { phone = it },
          label = { Text("Số điện thoại") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("customer_phone_input"),
        )
        OutlinedTextField(
          value = deviceNote,
          onValueChange = { deviceNote = it },
          label = { Text("Dòng máy / Ghi chú") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("customer_note_input"),
        )
      }
    },
    confirmButton = {
      Button(
        onClick = { onConfirm(name, phone, deviceNote) },
        modifier = Modifier.testTag("confirm_save_customer"),
      ) {
        Text("Lưu khách")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text("Hủy") }
    },
  )
}
