package com.khohangmin.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import com.khohangmin.ui.WarehouseUiState
import com.khohangmin.ui.theme.CrimsonLow
import com.khohangmin.ui.theme.CrimsonLowSoft
import com.khohangmin.ui.theme.ForestStock
import com.khohangmin.ui.theme.ForestStockSoft
import com.khohangmin.ui.theme.MonoMetricStyle
import com.khohangmin.ui.theme.MonoTagStyle
import com.shopcore.data.ProductEntity
import com.shopcore.ui.ShopConstants
import com.shopcore.ui.formatVnd

@Composable
fun KhoGoodsScreen(
  uiState: WarehouseUiState,
  onSearchChange: (String) -> Unit,
  onFilterChange: (String) -> Unit,
  onAdjustStock: (String, Int) -> Unit,
  onSaveProduct: (
    existingId: String?,
    sku: String,
    name: String,
    subCategory: String,
    shelfLocation: String,
    costPriceVnd: Long,
    sellPriceVnd: Long,
    stock: Int,
    minThreshold: Int,
    onSuccess: () -> Unit,
  ) -> Unit,
  onDeleteProduct: (ProductEntity) -> Unit,
  modifier: Modifier = Modifier,
) {
  var editingProduct by remember { mutableStateOf<ProductEntity?>(null) }
  var showFormDialog by remember { mutableStateOf(false) }

  val filters = listOf("Tất cả", "Phụ kiện", "Cường lực", "Linh kiện", "Pin", "Cáp sạc", "Sắp hết")

  Box(modifier = modifier.fillMaxSize()) {
    Column(
      modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Spacer(Modifier.height(4.dp))
      OutlinedTextField(
        value = uiState.goodsSearchQuery,
        onValueChange = onSearchChange,
        placeholder = { Text("Tìm tên hàng, mã SKU, vị trí kệ...") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        singleLine = true,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth().testTag("goods_search_input"),
      )

      LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        items(filters, key = { it }) { filter ->
          FilterChip(
            selected = uiState.goodsFilter == filter,
            onClick = { onFilterChange(filter) },
            label = { Text(filter) },
            modifier = Modifier.testTag("goods_filter_$filter"),
          )
        }
      }

      LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 88.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        if (uiState.filteredProducts.isEmpty()) {
          item(key = "goods_empty", contentType = "empty") {
            Card(
              shape = MaterialTheme.shapes.medium,
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
              border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
            ) {
              Text(
                text =
                  if (uiState.products.isEmpty())
                    "Kho chưa có mặt hàng nào. Bấm nút + bên dưới để thêm Phụ kiện, Cường lực, Pin hoặc Cáp sạc."
                  else "Không tìm thấy hàng hóa phù hợp với bộ lọc hiện tại.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth().padding(16.dp),
              )
            }
          }
        } else {
          items(uiState.filteredProducts, key = { it.id }, contentType = { "good" }) { product ->
            GoodsItemCard(
              product = product,
              onIncrement = { onAdjustStock(product.id, 1) },
              onDecrement = { onAdjustStock(product.id, -1) },
              onRestockFive = { onAdjustStock(product.id, 5) },
              onEdit = {
                editingProduct = product
                showFormDialog = true
              },
              onDelete = { onDeleteProduct(product) },
            )
          }
        }
      }
    }

    FloatingActionButton(
      onClick = {
        editingProduct = null
        showFormDialog = true
      },
      containerColor = MaterialTheme.colorScheme.primary,
      contentColor = MaterialTheme.colorScheme.onPrimary,
      modifier =
        Modifier.align(Alignment.BottomEnd).padding(16.dp).testTag("add_good_fab"),
    ) {
      Icon(Icons.Default.Add, contentDescription = "Thêm hàng hóa")
    }
  }

  if (showFormDialog) {
    GoodFormDialog(
      existing = editingProduct,
      onDismiss = { showFormDialog = false },
      onConfirm = { sku, name, subCategory, shelf, cost, sell, stock, threshold ->
        onSaveProduct(
          editingProduct?.id,
          sku,
          name,
          subCategory,
          shelf,
          cost,
          sell,
          stock,
          threshold,
        ) {
          showFormDialog = false
        }
      },
    )
  }
}

@Composable
private fun GoodsItemCard(
  product: ProductEntity,
  onIncrement: () -> Unit,
  onDecrement: () -> Unit,
  onRestockFive: () -> Unit,
  onEdit: () -> Unit,
  onDelete: () -> Unit,
) {
  val isLow = product.stock <= product.minThreshold
  val profitPerUnit = product.sellPriceVnd - product.costPriceVnd
  val categoryLabel =
    if (product.groupName == "Linh kiện") "Linh kiện · ${product.subCategory}"
    else product.subCategory

  Card(
    shape = MaterialTheme.shapes.medium,
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border =
      BorderStroke(
        1.dp,
        if (isLow) CrimsonLow.copy(alpha = 0.45f)
        else MaterialTheme.colorScheme.outline.copy(alpha = 0.55f),
      ),
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
            Text(
              text = categoryLabel,
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onPrimaryContainer,
              fontWeight = FontWeight.SemiBold,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            )
          }
          Text(
            text = "${product.sku} · ${product.shelfLocation}",
            style = MonoTagStyle,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }

        Surface(
          color = if (isLow) CrimsonLowSoft else ForestStockSoft,
          shape = RoundedCornerShape(99.dp),
        ) {
          Text(
            text = "Tồn: ${product.stock}",
            style = MonoTagStyle,
            color = if (isLow) CrimsonLow else ForestStock,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
          )
        }
      }

      Text(
        text = product.name,
        style = MaterialTheme.typography.titleMedium,
      )

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Column {
          Text(
            text = "Giá gốc",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
          Text(
            text = formatVnd(product.costPriceVnd),
            style = MonoTagStyle,
          )
        }
        Column {
          Text(
            text = "Giá bán",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
          Text(
            text = formatVnd(product.sellPriceVnd),
            style = MonoMetricStyle,
            color = MaterialTheme.colorScheme.primary,
          )
        }
        Column(horizontalAlignment = Alignment.End) {
          Text(
            text = "Lời/SP",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
          Text(
            text = "+${formatVnd(profitPerUnit)}",
            style = MonoTagStyle,
            color = ForestStock,
            fontWeight = FontWeight.Bold,
          )
        }
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
          IconButton(
            onClick = onDecrement,
            modifier = Modifier.size(48.dp).testTag("minus_stock_${product.id}"),
          ) {
            Icon(Icons.Default.Remove, contentDescription = "Giảm tồn 1")
          }
          IconButton(
            onClick = onIncrement,
            modifier = Modifier.size(48.dp).testTag("plus_stock_${product.id}"),
          ) {
            Icon(Icons.Default.Add, contentDescription = "Tăng tồn 1")
          }
          OutlinedButton(
            onClick = onRestockFive,
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
            modifier = Modifier.testTag("plus5_stock_${product.id}"),
          ) {
            Text("+5 nhập")
          }
        }

        Row {
          IconButton(
            onClick = onEdit,
            modifier = Modifier.size(48.dp).testTag("edit_product_${product.id}"),
          ) {
            Icon(Icons.Default.Edit, contentDescription = "Sửa hàng hóa")
          }
          IconButton(
            onClick = onDelete,
            modifier = Modifier.size(48.dp).testTag("delete_product_${product.id}"),
          ) {
            Icon(
              Icons.Default.DeleteOutline,
              contentDescription = "Xóa hàng hóa",
              tint = CrimsonLow,
            )
          }
        }
      }
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GoodFormDialog(
  existing: ProductEntity?,
  onDismiss: () -> Unit,
  onConfirm: (
    sku: String,
    name: String,
    subCategory: String,
    shelf: String,
    cost: Long,
    sell: Long,
    stock: Int,
    threshold: Int,
  ) -> Unit,
) {
  var name by remember { mutableStateOf(existing?.name.orEmpty()) }
  var sku by remember { mutableStateOf(existing?.sku.orEmpty()) }
  var subCategory by remember {
    mutableStateOf(existing?.subCategory ?: ShopConstants.GOOD_SUBCATEGORIES.first())
  }
  var shelf by remember { mutableStateOf(existing?.shelfLocation ?: "Kệ PK-01") }
  var costText by remember { mutableStateOf(existing?.costPriceVnd?.toString().orEmpty()) }
  var sellText by remember { mutableStateOf(existing?.sellPriceVnd?.toString().orEmpty()) }
  var stockText by remember { mutableStateOf(existing?.stock?.toString() ?: "10") }
  var thresholdText by remember { mutableStateOf(existing?.minThreshold?.toString() ?: "5") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(if (existing == null) "Thêm hàng hóa mới" else "Cập nhật hàng hóa") },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        Text(
          text = "Phân loại (Phụ kiện / Cường lực / Linh kiện)",
          style = MaterialTheme.typography.labelLarge,
        )
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
          ShopConstants.GOOD_SUBCATEGORIES.forEach { option ->
            val label =
              if (option == "Pin" || option == "Cáp sạc") "Linh kiện: $option" else option
            FilterChip(
              selected = subCategory == option,
              onClick = { subCategory = option },
              label = { Text(label) },
            )
          }
        }

        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text("Tên sản phẩm *") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("form_product_name"),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(
            value = sku,
            onValueChange = { sku = it },
            label = { Text("Mã SKU") },
            singleLine = true,
            modifier = Modifier.weight(1f),
          )
          OutlinedTextField(
            value = shelf,
            onValueChange = { shelf = it },
            label = { Text("Vị trí kệ") },
            singleLine = true,
            modifier = Modifier.weight(1f),
          )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(
            value = costText,
            onValueChange = { costText = it.filter(Char::isDigit) },
            label = { Text("Giá gốc (₫)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.weight(1f),
          )
          OutlinedTextField(
            value = sellText,
            onValueChange = { sellText = it.filter(Char::isDigit) },
            label = { Text("Giá bán (₫)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.weight(1f),
          )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(
            value = stockText,
            onValueChange = { stockText = it.filter(Char::isDigit) },
            label = { Text("Số lượng tồn") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.weight(1f),
          )
          OutlinedTextField(
            value = thresholdText,
            onValueChange = { thresholdText = it.filter(Char::isDigit) },
            label = { Text("Cảnh báo dưới") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.weight(1f),
          )
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          onConfirm(
            sku,
            name,
            subCategory,
            shelf,
            costText.toLongOrNull() ?: 0L,
            sellText.toLongOrNull() ?: (costText.toLongOrNull() ?: 0L),
            stockText.toIntOrNull() ?: 0,
            thresholdText.toIntOrNull() ?: 5,
          )
        },
        modifier = Modifier.testTag("confirm_save_product"),
      ) {
        Text("Lưu hàng hóa")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Hủy")
      }
    },
  )
}
