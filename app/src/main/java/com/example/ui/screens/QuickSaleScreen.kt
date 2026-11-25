package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.ui.HqAppleUiState
import com.example.ui.theme.CrimsonAlert
import com.example.ui.theme.CrimsonSoft
import com.example.ui.theme.EmeraldSoft
import com.example.ui.theme.EmeraldVerify
import com.example.ui.theme.MonoCodeStyle
import com.example.ui.theme.MonoPriceStyle
import com.shopcore.data.CustomerEntity
import com.shopcore.data.ProductEntity
import com.shopcore.data.ShopOrderEntity
import com.shopcore.ui.ShopConstants
import com.shopcore.ui.formatVnd

@Composable
fun QuickSaleScreen(
  uiState: HqAppleUiState,
  onSearchChange: (String) -> Unit,
  onCategoryChange: (String) -> Unit,
  onAddToCart: (ProductEntity) -> Unit,
  onDecrementCart: (String) -> Unit,
  onClearCart: () -> Unit,
  onCheckout: (
    customerName: String,
    customerPhone: String,
    warrantyType: String,
    paymentMethod: String,
    onCompleted: (ShopOrderEntity) -> Unit,
  ) -> Unit,
  modifier: Modifier = Modifier,
) {
  var showCheckoutDialog by remember { mutableStateOf(false) }
  var completedOrderReceipt by remember { mutableStateOf<ShopOrderEntity?>(null) }

  val categories = listOf("Tất cả", "Phụ kiện", "Cường lực", "Pin", "Cáp sạc")

  Column(modifier = modifier.fillMaxSize()) {
    Column(
      modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 16.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      OutlinedTextField(
        value = uiState.quickSaleSearchQuery,
        onValueChange = onSearchChange,
        placeholder = { Text("Tìm phụ kiện, cường lực, pin, cáp sạc...") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        singleLine = true,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth().testTag("quick_sale_search_input"),
      )

      LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(categories, key = { it }) { category ->
          FilterChip(
            selected = uiState.quickSaleCategory == category,
            onClick = { onCategoryChange(category) },
            label = { Text(category) },
            modifier = Modifier.testTag("quick_sale_filter_$category"),
          )
        }
      }

      LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        if (uiState.filteredQuickSaleProducts.isEmpty()) {
          item(key = "quick_sale_empty", contentType = "empty") {
            Card(
              shape = MaterialTheme.shapes.medium,
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
              border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
            ) {
              Text(
                text =
                  if (uiState.products.isEmpty())
                    "Kho chưa có hàng hóa. Hãy mở Kho Hàng Min để thêm Phụ kiện, Cường lực, Pin hoặc Cáp sạc."
                  else "Không tìm thấy sản phẩm phù hợp với từ khóa hoặc bộ lọc.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth().padding(16.dp),
              )
            }
          }
        } else {
          items(uiState.filteredQuickSaleProducts, key = { it.id }, contentType = { "product" }) { product ->
            val qtyInCart = uiState.cartQuantities[product.id] ?: 0
            QuickSaleProductCard(
              product = product,
              qtyInCart = qtyInCart,
              onAdd = { onAddToCart(product) },
              onDecrement = { onDecrementCart(product.id) },
            )
          }
        }
      }
    }

    if (uiState.cartItemCount > 0) {
      Surface(
        tonalElevation = 4.dp,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
      ) {
        Row(
          modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            IconButton(
              onClick = onClearCart,
              modifier = Modifier.size(48.dp).testTag("clear_quick_cart_btn"),
            ) {
              Icon(Icons.Default.DeleteSweep, contentDescription = "Xóa giỏ", tint = CrimsonAlert)
            }
            Column {
              Text(
                text = "${uiState.cartItemCount} sản phẩm đã chọn",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
              Text(
                text = formatVnd(uiState.cartTotalVnd),
                style = MonoPriceStyle,
                color = MaterialTheme.colorScheme.primary,
              )
            }
          }
          Button(
            onClick = { showCheckoutDialog = true },
            modifier = Modifier.testTag("open_quick_checkout_btn"),
          ) {
            Icon(Icons.Default.ShoppingBag, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.size(6.dp))
            Text("Thanh toán")
          }
        }
      }
    }
  }

  if (showCheckoutDialog) {
    QuickSaleCheckoutDialog(
      customers = uiState.customers,
      totalVnd = uiState.cartTotalVnd,
      itemCount = uiState.cartItemCount,
      onDismiss = { showCheckoutDialog = false },
      onConfirm = { name, phone, warranty, paymentMethod ->
        onCheckout(name, phone, warranty, paymentMethod) { order ->
          showCheckoutDialog = false
          completedOrderReceipt = order
        }
      },
    )
  }

  completedOrderReceipt?.let { order ->
    AlertDialog(
      onDismissRequest = { completedOrderReceipt = null },
      icon = {
        Icon(
          Icons.Default.CheckCircle,
          contentDescription = null,
          tint = EmeraldVerify,
          modifier = Modifier.size(36.dp),
        )
      },
      title = { Text("Đã xuất đơn ${order.orderCode}") },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
          Text("Khách hàng: ${order.customerName} (${order.customerPhone})")
          Text("Sản phẩm: ${order.summary}")
          Text("Bảo hành: ${order.warrantyType}")
          Text("Thanh toán: ${order.paymentMethod}")
          Text(
            text = "Tổng thu: ${formatVnd(order.totalVnd)}",
            style = MonoPriceStyle,
            color = MaterialTheme.colorScheme.primary,
          )
        }
      },
      confirmButton = {
        Button(onClick = { completedOrderReceipt = null }) { Text("Xong") }
      },
    )
  }
}

@Composable
private fun QuickSaleProductCard(
  product: ProductEntity,
  qtyInCart: Int,
  onAdd: () -> Unit,
  onDecrement: () -> Unit,
) {
  val outOfStock = product.stock <= 0
  Card(
    shape = MaterialTheme.shapes.medium,
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border =
      BorderStroke(
        1.dp,
        if (qtyInCart > 0) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.outline.copy(alpha = 0.55f),
      ),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(14.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Column(
        modifier = Modifier.weight(1f).padding(end = 10.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
      ) {
        Row(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = RoundedCornerShape(6.dp),
          ) {
            Text(
              text = product.subCategory,
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onPrimaryContainer,
              fontWeight = FontWeight.SemiBold,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            )
          }
          Surface(
            color = if (outOfStock) CrimsonSoft else EmeraldSoft,
            shape = RoundedCornerShape(6.dp),
          ) {
            Text(
              text = if (outOfStock) "Hết hàng" else "Kho: ${product.stock}",
              style = MonoCodeStyle,
              color = if (outOfStock) CrimsonAlert else EmeraldVerify,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            )
          }
        }
        Text(
          text = product.name,
          style = MaterialTheme.typography.titleMedium,
        )
        Text(
          text = formatVnd(product.sellPriceVnd),
          style = MonoPriceStyle,
          color = MaterialTheme.colorScheme.primary,
        )
      }

      if (qtyInCart == 0) {
        Button(
          onClick = onAdd,
          enabled = !outOfStock,
          modifier = Modifier.testTag("quick_add_${product.id}"),
        ) {
          Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(Modifier.size(4.dp))
          Text("Chọn")
        }
      } else {
        Row(verticalAlignment = Alignment.CenterVertically) {
          IconButton(
            onClick = onDecrement,
            modifier = Modifier.size(48.dp).testTag("quick_dec_${product.id}"),
          ) {
            Icon(Icons.Default.Remove, contentDescription = "Bớt 1")
          }
          Text(
            text = qtyInCart.toString(),
            style = MonoPriceStyle,
            modifier = Modifier.padding(horizontal = 4.dp),
          )
          IconButton(
            onClick = onAdd,
            modifier = Modifier.size(48.dp).testTag("quick_inc_${product.id}"),
          ) {
            Icon(Icons.Default.Add, contentDescription = "Thêm 1")
          }
        }
      }
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun QuickSaleCheckoutDialog(
  customers: List<CustomerEntity>,
  totalVnd: Long,
  itemCount: Int,
  onDismiss: () -> Unit,
  onConfirm: (
    customerName: String,
    customerPhone: String,
    warrantyType: String,
    paymentMethod: String,
  ) -> Unit,
) {
  var customerName by remember { mutableStateOf("") }
  var customerPhone by remember { mutableStateOf("") }
  var warrantyType by remember { mutableStateOf(ShopConstants.WARRANTY_OPTIONS[0]) }
  var paymentMethod by remember { mutableStateOf("Tiền mặt") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Thanh toán nhanh ($itemCount món)") },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        Text(
          text = "Tổng tiền: ${formatVnd(totalVnd)}",
          style = MonoPriceStyle,
          color = MaterialTheme.colorScheme.primary,
        )
        if (customers.isNotEmpty()) {
          Text("Chọn nhanh khách hàng", style = MaterialTheme.typography.labelLarge)
          LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(customers, key = { it.id }) { customer ->
              FilterChip(
                selected = customerPhone == customer.phone && customerName == customer.name,
                onClick = {
                  customerName = customer.name
                  customerPhone = customer.phone
                },
                label = { Text(customer.name) },
              )
            }
          }
        }
        OutlinedTextField(
          value = customerName,
          onValueChange = { customerName = it },
          label = { Text("Tên khách (để trống = Khách lẻ)") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("checkout_customer_name"),
        )
        OutlinedTextField(
          value = customerPhone,
          onValueChange = { customerPhone = it },
          label = { Text("Số điện thoại") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("checkout_customer_phone"),
        )

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

        Text("Thanh toán", style = MaterialTheme.typography.labelLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          listOf("Tiền mặt", "Chuyển khoản").forEach { method ->
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
        onClick = { onConfirm(customerName, customerPhone, warrantyType, paymentMethod) },
        modifier = Modifier.testTag("confirm_quick_checkout"),
      ) {
        Text("Xuất đơn")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text("Hủy") }
    },
  )
}
