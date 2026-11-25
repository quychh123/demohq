package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PhoneIphone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.HqAppleUiState
import com.example.ui.theme.EmeraldSoft
import com.example.ui.theme.EmeraldVerify
import com.example.ui.theme.MonoCodeStyle
import com.example.ui.theme.MonoPriceStyle
import com.example.ui.theme.WarmAmber
import com.example.ui.theme.WarmAmberSoft
import com.shopcore.data.CustomerEntity
import com.shopcore.data.ProductEntity
import com.shopcore.data.RepairTicketEntity
import com.shopcore.ui.ShopConstants
import com.shopcore.ui.formatShortDateTime
import com.shopcore.ui.formatVnd

@Composable
fun RepairIntakeScreen(
  uiState: HqAppleUiState,
  onSearchChange: (String) -> Unit,
  onStatusFilterChange: (String) -> Unit,
  onCreateTicket: (
    customerName: String,
    customerPhone: String,
    deviceModel: String,
    imei: String,
    issueDescription: String,
    partId: String?,
    manualCostPriceVnd: Long,
    totalPriceVnd: Long,
    warrantyType: String,
    onSuccess: (RepairTicketEntity) -> Unit,
  ) -> Unit,
  onUpdateStatus: (ticketId: String, newStatus: String, paymentMethod: String) -> Unit,
  modifier: Modifier = Modifier,
) {
  var showCreateDialog by remember { mutableStateOf(false) }
  var completingTicket by remember { mutableStateOf<RepairTicketEntity?>(null) }

  val statusFilters = listOf("Tất cả", "Đang nhận", "Đang sửa", "Hoàn tất")

  Box(modifier = modifier.fillMaxSize()) {
    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      item(key = "repair_hero", contentType = "hero") {
        Card(
          shape = MaterialTheme.shapes.large,
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
        ) {
          Box(modifier = Modifier.fillMaxWidth().height(144.dp)) {
            Image(
              painter = painterResource(id = R.drawable.img_hq_apple_hero),
              contentDescription = "HQ Apple Tiếp nhận sửa chữa",
              contentScale = ContentScale.Crop,
              modifier = Modifier.fillMaxSize(),
            )
            Box(
              modifier =
                Modifier.fillMaxSize()
                  .background(
                    Brush.verticalGradient(
                      listOf(Color(0x660F172A), Color(0xDF0F172A))
                    )
                  )
                  .padding(16.dp)
            ) {
              Column(
                modifier = Modifier.align(Alignment.BottomStart),
                verticalArrangement = Arrangement.spacedBy(5.dp),
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
                      imageVector = Icons.Default.Sync,
                      contentDescription = null,
                      tint = Color.White,
                      modifier = Modifier.size(14.dp),
                    )
                    Text(
                      text = "HQ APPLE · ĐỒNG BỘ KHO HÀNG MIN",
                      style = MaterialTheme.typography.labelSmall,
                      color = Color.White,
                      fontWeight = FontWeight.Bold,
                    )
                  }
                }
                Text(
                  text = "Tiếp nhận sửa chữa điện thoại",
                  style = MaterialTheme.typography.titleLarge,
                  color = Color.White,
                )
                Text(
                  text = "Gắn linh kiện Pin/Cáp sạc từ kho · Bảo hành 1/2/3 tháng, bao test 7 ngày",
                  style = MaterialTheme.typography.bodyMedium,
                  color = Color(0xFFE5E7EB),
                )
              }
            }
          }
        }
      }

      item(key = "repair_search", contentType = "search") {
        OutlinedTextField(
          value = uiState.repairSearchQuery,
          onValueChange = onSearchChange,
          placeholder = { Text("Tìm mã phiếu, tên khách, SĐT, dòng máy, IMEI...") },
          leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
          singleLine = true,
          shape = MaterialTheme.shapes.medium,
          modifier = Modifier.fillMaxWidth().testTag("repair_search_input"),
        )
      }

      item(key = "repair_filters", contentType = "filters") {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          items(statusFilters, key = { it }) { status ->
            FilterChip(
              selected = uiState.repairStatusFilter == status,
              onClick = { onStatusFilterChange(status) },
              label = { Text(status) },
              modifier = Modifier.testTag("repair_status_filter_$status"),
            )
          }
        }
      }

      if (uiState.filteredRepairs.isEmpty()) {
        item(key = "repair_empty", contentType = "empty") {
          Card(
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
          ) {
            Text(
              text =
                if (uiState.repairTickets.isEmpty())
                  "Chưa có phiếu tiếp nhận sửa chữa nào. Bấm nút + bên dưới để lập phiếu mới."
                else "Không tìm thấy phiếu sửa chữa phù hợp.",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.fillMaxWidth().padding(16.dp),
            )
          }
        }
      } else {
        items(uiState.filteredRepairs, key = { it.id }, contentType = { "ticket" }) { ticket ->
          RepairTicketCard(
            ticket = ticket,
            onStartRepair = { onUpdateStatus(ticket.id, "Đang sửa", "Thanh toán khi giao") },
            onCompleteRepair = { completingTicket = ticket },
          )
        }
      }
    }

    FloatingActionButton(
      onClick = { showCreateDialog = true },
      containerColor = MaterialTheme.colorScheme.primary,
      contentColor = MaterialTheme.colorScheme.onPrimary,
      modifier =
        Modifier.align(Alignment.BottomEnd).padding(16.dp).testTag("add_repair_ticket_fab"),
    ) {
      Icon(Icons.Default.Add, contentDescription = "Tiếp nhận sửa chữa")
    }
  }

  if (showCreateDialog) {
    CreateRepairTicketDialog(
      customers = uiState.customers,
      products = uiState.products,
      onDismiss = { showCreateDialog = false },
      onConfirm = { name, phone, device, imei, issue, partId, manualCost, total, warranty ->
        onCreateTicket(name, phone, device, imei, issue, partId, manualCost, total, warranty) {
          showCreateDialog = false
        }
      },
    )
  }

  completingTicket?.let { ticket ->
    CompleteRepairPaymentDialog(
      ticket = ticket,
      onDismiss = { completingTicket = null },
      onConfirm = { method ->
        onUpdateStatus(ticket.id, "Hoàn tất", method)
        completingTicket = null
      },
    )
  }
}

@Composable
private fun RepairTicketCard(
  ticket: RepairTicketEntity,
  onStartRepair: () -> Unit,
  onCompleteRepair: () -> Unit,
) {
  val (statusBg, statusFg) =
    when (ticket.status) {
      "Hoàn tất" -> EmeraldSoft to EmeraldVerify
      "Đang sửa" -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.primary
      else -> WarmAmberSoft to WarmAmber
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
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          Text(
            text = ticket.ticketCode,
            style = MonoCodeStyle,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
          )
          Surface(color = statusBg, shape = RoundedCornerShape(99.dp)) {
            Text(
              text = ticket.status,
              style = MaterialTheme.typography.labelSmall,
              color = statusFg,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
            )
          }
        }
        Text(
          text = formatShortDateTime(ticket.createdAtMillis),
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }

      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
      ) {
        Icon(
          imageVector = Icons.Default.PhoneIphone,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(18.dp),
        )
        Text(
          text = ticket.deviceModel,
          style = MaterialTheme.typography.titleMedium,
        )
      }

      Text(
        text = "Khách: ${ticket.customerName} · ${ticket.customerPhone}",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
      Text(
        text = "IMEI/Serial: ${ticket.imei}",
        style = MonoCodeStyle,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
      Text(
        text = "Lỗi: ${ticket.issueDescription}",
        style = MaterialTheme.typography.bodyLarge,
      )

      Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.small,
      ) {
        Row(
          modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "Linh kiện / Hạng mục",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
              text = ticket.partName,
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.SemiBold,
            )
          }
          Text(
            text = formatVnd(ticket.totalPriceVnd),
            style = MonoPriceStyle,
            color = MaterialTheme.colorScheme.primary,
          )
        }
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Surface(
          color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
          shape = RoundedCornerShape(8.dp),
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
          ) {
            Icon(
              imageVector = Icons.Default.VerifiedUser,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(14.dp),
            )
            Text(
              text = ticket.warrantyType,
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onPrimaryContainer,
              fontWeight = FontWeight.SemiBold,
            )
          }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          if (ticket.status == "Đang nhận") {
            OutlinedButton(
              onClick = onStartRepair,
              modifier = Modifier.testTag("start_repair_${ticket.id}"),
            ) {
              Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(Modifier.size(4.dp))
              Text("Đang sửa")
            }
          }
          if (ticket.status != "Hoàn tất") {
            Button(
              onClick = onCompleteRepair,
              modifier = Modifier.testTag("complete_repair_${ticket.id}"),
            ) {
              Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(Modifier.size(4.dp))
              Text("Hoàn tất")
            }
          }
        }
      }
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CreateRepairTicketDialog(
  customers: List<CustomerEntity>,
  products: List<ProductEntity>,
  onDismiss: () -> Unit,
  onConfirm: (
    customerName: String,
    customerPhone: String,
    deviceModel: String,
    imei: String,
    issueDescription: String,
    partId: String?,
    manualCostPriceVnd: Long,
    totalPriceVnd: Long,
    warrantyType: String,
  ) -> Unit,
) {
  var customerName by remember { mutableStateOf("") }
  var customerPhone by remember { mutableStateOf("") }
  var deviceModel by remember { mutableStateOf("") }
  var imei by remember { mutableStateOf("") }
  var issueDescription by remember { mutableStateOf("") }
  var selectedPart by remember { mutableStateOf<ProductEntity?>(null) }
  var manualCostText by remember { mutableStateOf("0") }
  var totalPriceText by remember { mutableStateOf("") }
  var warrantyType by remember { mutableStateOf(ShopConstants.WARRANTY_OPTIONS[1]) }

  val availableParts =
    remember(products) {
      products.filter { it.stock > 0 }
        .sortedByDescending { it.groupName == "Linh kiện" }
    }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Phiếu tiếp nhận sửa chữa") },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        if (customers.isNotEmpty()) {
          Text("Chọn nhanh khách quen", style = MaterialTheme.typography.labelLarge)
          LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(customers, key = { it.id }) { customer ->
              FilterChip(
                selected = customerPhone == customer.phone && customerName == customer.name,
                onClick = {
                  customerName = customer.name
                  customerPhone = customer.phone
                  if (deviceModel.isBlank()) {
                    deviceModel = customer.deviceNote.substringBefore("·").trim()
                  }
                },
                label = { Text(customer.name) },
              )
            }
          }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(
            value = customerName,
            onValueChange = { customerName = it },
            label = { Text("Tên khách hàng") },
            singleLine = true,
            modifier = Modifier.weight(1f).testTag("repair_customer_name"),
          )
          OutlinedTextField(
            value = customerPhone,
            onValueChange = { customerPhone = it },
            label = { Text("SĐT") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            singleLine = true,
            modifier = Modifier.weight(1f).testTag("repair_customer_phone"),
          )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(
            value = deviceModel,
            onValueChange = { deviceModel = it },
            label = { Text("Dòng máy *") },
            placeholder = { Text("iPhone 14 Pro Max") },
            singleLine = true,
            modifier = Modifier.weight(1f).testTag("repair_device_model"),
          )
          OutlinedTextField(
            value = imei,
            onValueChange = { imei = it },
            label = { Text("IMEI / Serial") },
            singleLine = true,
            modifier = Modifier.weight(1f),
          )
        }

        OutlinedTextField(
          value = issueDescription,
          onValueChange = { issueDescription = it },
          label = { Text("Tình trạng lỗi tiếp nhận *") },
          placeholder = { Text("VD: Pin chai 70%, hỏng chân sạc...") },
          modifier = Modifier.fillMaxWidth().testTag("repair_issue_input"),
        )

        Text("Linh kiện từ Kho Hàng Min (tùy chọn)", style = MaterialTheme.typography.labelLarge)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          item {
            FilterChip(
              selected = selectedPart == null,
              onClick = { selectedPart = null },
              label = { Text("Không dùng LK kho") },
            )
          }
          items(availableParts, key = { it.id }) { part ->
            FilterChip(
              selected = selectedPart?.id == part.id,
              onClick = {
                selectedPart = part
                if (totalPriceText.isBlank()) {
                  totalPriceText = part.sellPriceVnd.toString()
                }
              },
              label = { Text("${part.subCategory}: ${part.name.take(22)} (${part.stock})") },
            )
          }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          if (selectedPart == null) {
            OutlinedTextField(
              value = manualCostText,
              onValueChange = { manualCostText = it.filter(Char::isDigit) },
              label = { Text("Tiền gốc (₫)") },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
              singleLine = true,
              modifier = Modifier.weight(1f),
            )
          } else {
            Surface(
              modifier = Modifier.weight(1f),
              color = MaterialTheme.colorScheme.surfaceVariant,
              shape = MaterialTheme.shapes.small,
            ) {
              Column(modifier = Modifier.padding(10.dp)) {
                Text("Gốc kho tự trừ", style = MaterialTheme.typography.labelSmall)
                Text(formatVnd(selectedPart!!.costPriceVnd), style = MonoCodeStyle)
              }
            }
          }
          OutlinedTextField(
            value = totalPriceText,
            onValueChange = { totalPriceText = it.filter(Char::isDigit) },
            label = { Text("Giá báo khách (₫) *") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.weight(1f).testTag("repair_total_price_input"),
          )
        }

        Text("Chế độ bảo hành", style = MaterialTheme.typography.labelLarge)
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
          ShopConstants.WARRANTY_OPTIONS.forEach { option ->
            FilterChip(
              selected = warrantyType == option,
              onClick = { warrantyType = option },
              label = { Text(option) },
            )
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          onConfirm(
            customerName,
            customerPhone,
            deviceModel,
            imei,
            issueDescription,
            selectedPart?.id,
            manualCostText.toLongOrNull() ?: 0L,
            totalPriceText.toLongOrNull() ?: 0L,
            warrantyType,
          )
        },
        modifier = Modifier.testTag("confirm_create_repair_btn"),
      ) {
        Text("Lập phiếu nhận")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text("Hủy") }
    },
  )
}

@Composable
private fun CompleteRepairPaymentDialog(
  ticket: RepairTicketEntity,
  onDismiss: () -> Unit,
  onConfirm: (paymentMethod: String) -> Unit,
) {
  var paymentMethod by remember { mutableStateOf("Tiền mặt") }
  val methods = listOf("Tiền mặt", "Chuyển khoản")

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Hoàn tất & Thu tiền ${ticket.ticketCode}") },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
          text = "Máy: ${ticket.deviceModel} · Khách: ${ticket.customerName}",
          style = MaterialTheme.typography.bodyLarge,
        )
        Text(
          text = "Tổng thu: ${formatVnd(ticket.totalPriceVnd)} (${ticket.warrantyType})",
          style = MonoPriceStyle,
          color = MaterialTheme.colorScheme.primary,
        )
        Text("Hình thức thanh toán", style = MaterialTheme.typography.labelLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          methods.forEach { method ->
            FilterChip(
              selected = paymentMethod == method,
              onClick = { paymentMethod = method },
              label = { Text(method) },
            )
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = { onConfirm(paymentMethod) },
        modifier = Modifier.testTag("confirm_complete_repair_payment"),
      ) {
        Text("Xác nhận thu tiền")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text("Đóng") }
    },
  )
}
