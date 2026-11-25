package com.khohangmin.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Cable
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.PhoneIphone
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.khohangmin.R
import com.khohangmin.ui.WarehouseUiState
import com.khohangmin.ui.theme.CrimsonLow
import com.khohangmin.ui.theme.CrimsonLowSoft
import com.khohangmin.ui.theme.ForestStock
import com.khohangmin.ui.theme.ForestStockSoft
import com.khohangmin.ui.theme.MonoMetricStyle
import com.khohangmin.ui.theme.MonoTagStyle
import com.shopcore.data.ProductEntity
import com.shopcore.ui.ShopConstants
import com.shopcore.ui.formatCompactVnd
import com.shopcore.ui.formatVnd

@Composable
fun KhoDashboardScreen(
  uiState: WarehouseUiState,
  onQuickRestock: (String, Int) -> Unit,
  onNavigateToCategory: (String) -> Unit,
  onNavigateToCashbook: () -> Unit,
  modifier: Modifier = Modifier,
) {
  LazyColumn(
    modifier = modifier.fillMaxSize(),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp),
  ) {
    item {
      Card(
        shape = MaterialTheme.shapes.large,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
      ) {
        Box(modifier = Modifier.fillMaxWidth().height(152.dp)) {
          Image(
            painter = painterResource(id = R.drawable.img_kho_min_hero),
            contentDescription = "Kho Hàng Min đồng bộ HQ Apple",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
          )
          Box(
            modifier =
              Modifier.fillMaxSize()
                .background(
                  Brush.verticalGradient(
                    colors =
                      listOf(
                        Color(0x880F172A),
                        Color(0xDD0F172A),
                      )
                  )
                )
                .padding(16.dp)
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
                    imageVector = Icons.Default.Sync,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp),
                  )
                  Text(
                    text = "ĐỒNG BỘ SONG SONG HQ APPLE",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                  )
                }
              }
              Text(
                text = "Kho Hàng Min",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
              )
              Text(
                text = "Quản lý Phụ kiện · Cường lực · Linh kiện (Pin, Cáp sạc) & Sổ quỹ",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFE2E8F0),
              )
            }
          }
        }
      }
    }

    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        KpiCard(
          title = "Tổng mã hàng",
          value = "${uiState.totalSkus} mã",
          sub = "${uiState.totalUnits} chiếc trong kho",
          icon = Icons.Default.Inventory2,
          modifier = Modifier.weight(1f),
        )
        KpiCard(
          title = "Vốn tồn kho",
          value = formatCompactVnd(uiState.inventoryCostVnd),
          sub = "Giá gốc hàng tồn",
          icon = Icons.Default.Savings,
          modifier = Modifier.weight(1f),
        )
      }
    }

    item {
      Card(
        modifier =
          Modifier.fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onNavigateToCashbook)
            .testTag("dashboard_cashbook_card"),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
      ) {
        Row(
          modifier = Modifier.fillMaxWidth().padding(16.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                contentDescription = null,
                tint = ForestStock,
                modifier = Modifier.size(18.dp),
              )
              Text(
                text = "Sổ quỹ · Gốc & Lời",
                style = MaterialTheme.typography.titleMedium,
              )
            }
            Text(
              text =
                "Thu: ${formatCompactVnd(uiState.totalRevenueVnd)} · Gốc: ${formatCompactVnd(uiState.totalCostVnd)}",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
          Column(horizontalAlignment = Alignment.End) {
            Text(
              text = "Tổng tiền lời",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
              text = formatVnd(uiState.totalProfitVnd),
              style = MonoMetricStyle,
              color = if (uiState.totalProfitVnd >= 0) ForestStock else CrimsonLow,
            )
          }
        }
      }
    }

    item {
      Text(
        text = "Nhóm hàng hóa trong kho",
        style = MaterialTheme.typography.titleMedium,
      )
    }

    item {
      val subCategories = ShopConstants.GOOD_SUBCATEGORIES
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          CategorySummaryCard(
            subCategory = subCategories[0], // Phụ kiện
            products = uiState.products.filter { it.subCategory == subCategories[0] },
            icon = Icons.Default.Headphones,
            onClick = { onNavigateToCategory(subCategories[0]) },
            modifier = Modifier.weight(1f),
          )
          CategorySummaryCard(
            subCategory = subCategories[1], // Cường lực
            products = uiState.products.filter { it.subCategory == subCategories[1] },
            icon = Icons.Default.PhoneIphone,
            onClick = { onNavigateToCategory(subCategories[1]) },
            modifier = Modifier.weight(1f),
          )
        }
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          CategorySummaryCard(
            subCategory = subCategories[2], // Pin
            products = uiState.products.filter { it.subCategory == subCategories[2] },
            icon = Icons.Default.BatteryChargingFull,
            badgeLabel = "Linh kiện",
            onClick = { onNavigateToCategory(subCategories[2]) },
            modifier = Modifier.weight(1f),
          )
          CategorySummaryCard(
            subCategory = subCategories[3], // Cáp sạc
            products = uiState.products.filter { it.subCategory == subCategories[3] },
            icon = Icons.Default.Cable,
            badgeLabel = "Linh kiện",
            onClick = { onNavigateToCategory(subCategories[3]) },
            modifier = Modifier.weight(1f),
          )
        }
      }
    }

    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          Icon(
            imageVector = Icons.Default.WarningAmber,
            contentDescription = null,
            tint = CrimsonLow,
            modifier = Modifier.size(18.dp),
          )
          Text(
            text = "Cảnh báo sắp hết hàng (${uiState.lowStockItems.size})",
            style = MaterialTheme.typography.titleMedium,
          )
        }
      }
    }

    if (uiState.lowStockItems.isEmpty()) {
      item {
        Card(
          shape = MaterialTheme.shapes.medium,
          colors = CardDefaults.cardColors(containerColor = ForestStockSoft),
        ) {
          Text(
            text =
              if (uiState.products.isEmpty())
                "Kho chưa có hàng hóa. Vào tab Hàng hóa để thêm Phụ kiện, Cường lực hoặc Linh kiện (Pin, Cáp sạc)."
              else "Tất cả mặt hàng đều đủ tồn kho an toàn.",
            style = MaterialTheme.typography.bodyMedium,
            color = ForestStock,
            modifier = Modifier.fillMaxWidth().padding(16.dp),
          )
        }
      }
    } else {
      items(uiState.lowStockItems, key = { it.id }) { item ->
        LowStockAlertCard(
          product = item,
          onRestock = { onQuickRestock(item.id, 5) },
        )
      }
    }
  }
}

@Composable
private fun KpiCard(
  title: String,
  value: String,
  sub: String,
  icon: ImageVector,
  modifier: Modifier = Modifier,
) {
  Card(
    modifier = modifier,
    shape = MaterialTheme.shapes.medium,
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
  ) {
    Column(
      modifier = Modifier.padding(14.dp),
      verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
      ) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(18.dp),
        )
        Text(
          text = title,
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
      Text(
        text = value,
        style = MonoMetricStyle,
        color = MaterialTheme.colorScheme.onSurface,
      )
      Text(
        text = sub,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
  }
}

@Composable
private fun CategorySummaryCard(
  subCategory: String,
  products: List<ProductEntity>,
  icon: ImageVector,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  badgeLabel: String? = null,
) {
  val totalUnits = products.sumOf { it.stock }
  val totalCost = products.sumOf { it.costPriceVnd * it.stock }
  Card(
    modifier =
      modifier
        .clip(MaterialTheme.shapes.medium)
        .clickable(onClick = onClick)
        .testTag("category_card_$subCategory"),
    shape = MaterialTheme.shapes.medium,
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.55f)),
  ) {
    Column(
      modifier = Modifier.padding(12.dp),
      verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Icon(
          imageVector = icon,
          contentDescription = subCategory,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(20.dp),
        )
        if (badgeLabel != null) {
          Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = RoundedCornerShape(99.dp),
          ) {
            Text(
              text = badgeLabel,
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onPrimaryContainer,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            )
          }
        }
      }
      Text(
        text = subCategory,
        style = MaterialTheme.typography.titleMedium,
      )
      Text(
        text = "${products.size} mã · $totalUnits cái",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
      Text(
        text = "Vốn: ${formatCompactVnd(totalCost)}",
        style = MonoTagStyle,
        color = MaterialTheme.colorScheme.onSurface,
      )
    }
  }
}

@Composable
private fun LowStockAlertCard(
  product: ProductEntity,
  onRestock: () -> Unit,
) {
  Card(
    shape = MaterialTheme.shapes.medium,
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = BorderStroke(1.dp, CrimsonLow.copy(alpha = 0.35f)),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(14.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Column(
        modifier = Modifier.weight(1f).padding(end = 10.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
      ) {
        Row(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Surface(color = CrimsonLowSoft, shape = RoundedCornerShape(6.dp)) {
            Text(
              text = "Còn ${product.stock}",
              style = MonoTagStyle,
              color = CrimsonLow,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            )
          }
          Text(
            text = "${product.groupName} · ${product.subCategory}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
        Text(
          text = product.name,
          style = MaterialTheme.typography.titleMedium,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
        Text(
          text = "Mã ${product.sku} · Gốc ${formatVnd(product.costPriceVnd)}",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
      FilledTonalButton(
        onClick = onRestock,
        colors =
          ButtonDefaults.filledTonalButtonColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
          ),
        modifier = Modifier.testTag("restock_btn_${product.id}"),
      ) {
        Icon(
          imageVector = Icons.Default.Add,
          contentDescription = null,
          modifier = Modifier.size(16.dp),
        )
        Spacer(Modifier.size(4.dp))
        Text("5")
      }
    }
  }
}
