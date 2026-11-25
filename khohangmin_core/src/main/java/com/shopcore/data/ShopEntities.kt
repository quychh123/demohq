package com.shopcore.data

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.PrimaryKey

@Immutable
@Entity(tableName = "shop_products")
data class ProductEntity(
  @PrimaryKey val id: String,
  val sku: String,
  val name: String,
  val groupName: String, // "Phụ kiện", "Cường lực", "Linh kiện"
  val subCategory: String, // "Phụ kiện", "Cường lực", "Pin", "Cáp sạc"
  val shelfLocation: String,
  val costPriceVnd: Long,
  val sellPriceVnd: Long,
  val stock: Int,
  val minThreshold: Int,
  val updatedAtMillis: Long,
)

@Immutable
@Entity(tableName = "shop_customers")
data class CustomerEntity(
  @PrimaryKey val id: String,
  val name: String,
  val phone: String,
  val deviceNote: String,
  val totalSpentVnd: Long,
  val orderCount: Int,
  val createdAtMillis: Long,
)

@Immutable
@Entity(tableName = "repair_tickets")
data class RepairTicketEntity(
  @PrimaryKey val id: String,
  val ticketCode: String,
  val customerId: String,
  val customerName: String,
  val customerPhone: String,
  val deviceModel: String,
  val imei: String,
  val issueDescription: String,
  val partId: String?,
  val partName: String,
  val costPriceVnd: Long,
  val totalPriceVnd: Long,
  val warrantyType: String, // "Bao test 7 ngày", "Bảo hành 1 tháng", "Bảo hành 2 tháng", "Bảo hành 3 tháng", "Không bảo hành"
  val status: String, // "Đang nhận", "Đang sửa", "Hoàn tất"
  val createdAtMillis: Long,
  val updatedAtMillis: Long,
)

@Immutable
@Entity(tableName = "shop_orders")
data class ShopOrderEntity(
  @PrimaryKey val id: String,
  val orderCode: String,
  val orderType: String, // "SALE" or "REPAIR"
  val customerId: String,
  val customerName: String,
  val customerPhone: String,
  val summary: String,
  val warrantyType: String,
  val paymentMethod: String,
  val status: String, // "Đang nhận", "Đang sửa", "Hoàn tất"
  val costTotalVnd: Long,
  val totalVnd: Long,
  val profitVnd: Long,
  val createdAtMillis: Long,
)

@Immutable
@Entity(tableName = "cashbook_entries")
data class CashbookEntryEntity(
  @PrimaryKey val id: String,
  val code: String,
  val entryType: String, // "SALE", "REPAIR", "INCOME", "EXPENSE"
  val title: String,
  val note: String,
  val revenueVnd: Long,
  val costVnd: Long,
  val profitVnd: Long,
  val createdAtMillis: Long,
)
