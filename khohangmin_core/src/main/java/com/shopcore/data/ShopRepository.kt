package com.shopcore.data

import android.content.Context
import androidx.room.withTransaction
import com.shopcore.backup.BackupSummary
import com.shopcore.backup.ShopBackupCodec
import com.shopcore.backup.ShopBackupSnapshot
import com.shopcore.ui.ShopConstants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

data class QuickCartLine(
  val product: ProductEntity,
  val quantity: Int,
)

class ShopRepository(
  private val database: ShopDatabase,
) {
  private val dao = database.dao()

  val products: Flow<List<ProductEntity>> = dao.observeProducts()
  val customers: Flow<List<CustomerEntity>> = dao.observeCustomers()
  val repairTickets: Flow<List<RepairTicketEntity>> = dao.observeRepairTickets()
  val orders: Flow<List<ShopOrderEntity>> = dao.observeOrders()
  val cashbookEntries: Flow<List<CashbookEntryEntity>> = dao.observeCashbookEntries()

  suspend fun purgeLegacySampleDataOnce(context: Context) {
    withContext(Dispatchers.IO) {
      val prefs =
        context.applicationContext.getSharedPreferences(
          "hq_kho_cleanup_prefs",
          Context.MODE_PRIVATE,
        )
      if (prefs.getBoolean("purged_sample_v1", false)) return@withContext
      database.withTransaction {
        dao.deleteCashbookEntriesByIds(LEGACY_SAMPLE_CASHBOOK_IDS)
        dao.deleteOrdersByIds(LEGACY_SAMPLE_ORDER_IDS)
        dao.deleteRepairTicketsByIds(LEGACY_SAMPLE_REPAIR_IDS)
        dao.deleteCustomersByIds(LEGACY_SAMPLE_CUSTOMER_IDS)
        dao.deleteProductsByIds(LEGACY_SAMPLE_PRODUCT_IDS)
      }
      prefs.edit().putBoolean("purged_sample_v1", true).apply()
    }
  }

  suspend fun clearAllData() {
    database.withTransaction {
      dao.clearCashbook()
      dao.clearOrders()
      dao.clearRepairTickets()
      dao.clearCustomers()
      dao.clearProducts()
    }
  }

  companion object {
    private val LEGACY_SAMPLE_PRODUCT_IDS =
      listOf(
        "prd_pk_op16pm",
        "prd_pk_op15",
        "prd_pk_cu20w",
        "prd_pk_tainghe",
        "prd_cl_kk16pm",
        "prd_cl_kk15",
        "prd_cl_1314",
        "prd_cl_cam16",
        "prd_pin_13pm",
        "prd_pin_12p",
        "prd_pin_11",
        "prd_cap_c2l",
        "prd_cap_c2c",
        "prd_cap_cum14",
      )
    private val LEGACY_SAMPLE_CUSTOMER_IDS = listOf("cus_01", "cus_02", "cus_03", "cus_04")
    private val LEGACY_SAMPLE_REPAIR_IDS = listOf("rep_01", "rep_02", "rep_03")
    private val LEGACY_SAMPLE_ORDER_IDS = listOf("ord_01", "ord_02", "ord_03", "ord_04")
    private val LEGACY_SAMPLE_CASHBOOK_IDS = listOf("cb_01", "cb_02", "cb_03", "cb_04")
  }

  private suspend fun resolveOrCreateCustomer(
    customerName: String,
    customerPhone: String,
    deviceNote: String,
    addSpentVnd: Long,
    incrementOrderCount: Int,
  ): CustomerEntity {
    val cleanPhone = customerPhone.trim()
    val cleanName = customerName.trim().ifBlank { "Khách lẻ" }
    val existing =
      if (cleanPhone.isNotBlank()) dao.getCustomerByPhone(cleanPhone) else null
    return if (existing != null) {
      val updated =
        existing.copy(
          name = if (cleanName != "Khách lẻ") cleanName else existing.name,
          deviceNote = deviceNote.ifBlank { existing.deviceNote },
          totalSpentVnd = existing.totalSpentVnd + addSpentVnd,
          orderCount = existing.orderCount + incrementOrderCount,
        )
      dao.insertCustomer(updated)
      updated
    } else {
      val created =
        CustomerEntity(
          id = "cus_${UUID.randomUUID().toString().take(8)}",
          name = cleanName,
          phone = cleanPhone.ifBlank { "Chưa có SĐT" },
          deviceNote = deviceNote.ifBlank { "Khách cửa hàng HQ Apple" },
          totalSpentVnd = addSpentVnd,
          orderCount = incrementOrderCount,
          createdAtMillis = System.currentTimeMillis(),
        )
      dao.insertCustomer(created)
      created
    }
  }

  suspend fun saveProduct(
    existingId: String?,
    sku: String,
    name: String,
    subCategory: String,
    shelfLocation: String,
    costPriceVnd: Long,
    sellPriceVnd: Long,
    stock: Int,
    minThreshold: Int,
  ): Result<Unit> {
    return try {
      require(name.isNotBlank()) { "Vui lòng nhập tên hàng hóa" }
      require(stock >= 0) { "Tồn kho không hợp lệ" }
      val now = System.currentTimeMillis()
      val groupName = ShopConstants.groupForSubCategory(subCategory)
      val prefix =
        when (subCategory) {
          "Pin" -> "PIN"
          "Cáp sạc" -> "CAP"
          "Cường lực" -> "CL"
          else -> "PK"
        }
      val cleanSku = sku.trim().ifBlank { "$prefix-${(now % 10000L)}" }.uppercase()
      val entity =
        ProductEntity(
          id = existingId ?: "prd_${UUID.randomUUID().toString().take(8)}",
          sku = cleanSku,
          name = name.trim(),
          groupName = groupName,
          subCategory = subCategory,
          shelfLocation = shelfLocation.trim().ifBlank { "Kệ 01" },
          costPriceVnd = costPriceVnd.coerceAtLeast(0L),
          sellPriceVnd = sellPriceVnd.coerceAtLeast(costPriceVnd),
          stock = stock,
          minThreshold = minThreshold.coerceAtLeast(0),
          updatedAtMillis = now,
        )
      dao.insertProduct(entity)
      Result.success(Unit)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun adjustProductStock(productId: String, delta: Int) {
    database.withTransaction {
      val item = dao.getProductById(productId) ?: return@withTransaction
      val next = (item.stock + delta).coerceAtLeast(0)
      dao.updateProductStock(productId, next, System.currentTimeMillis())
    }
  }

  suspend fun deleteProduct(item: ProductEntity) {
    dao.deleteProduct(item)
  }

  suspend fun saveCustomer(
    existingId: String?,
    name: String,
    phone: String,
    deviceNote: String,
  ): Result<Unit> {
    return try {
      require(name.isNotBlank()) { "Vui lòng nhập tên khách hàng" }
      val existing = if (existingId != null) dao.getCustomerById(existingId) else null
      val entity =
        CustomerEntity(
          id = existingId ?: "cus_${UUID.randomUUID().toString().take(8)}",
          name = name.trim(),
          phone = phone.trim().ifBlank { "Chưa có SĐT" },
          deviceNote = deviceNote.trim().ifBlank { "Khách hàng HQ Apple" },
          totalSpentVnd = existing?.totalSpentVnd ?: 0L,
          orderCount = existing?.orderCount ?: 0,
          createdAtMillis = existing?.createdAtMillis ?: System.currentTimeMillis(),
        )
      dao.insertCustomer(entity)
      Result.success(Unit)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun deleteCustomer(customer: CustomerEntity) {
    dao.deleteCustomer(customer)
  }

  suspend fun addManualCashbookEntry(
    isIncome: Boolean,
    title: String,
    amountVnd: Long,
    note: String,
  ): Result<Unit> {
    return try {
      require(title.isNotBlank()) { "Vui lòng nhập nội dung phiếu quỹ" }
      require(amountVnd > 0L) { "Số tiền phải lớn hơn 0" }
      val now = System.currentTimeMillis()
      val codePrefix = if (isIncome) "PT" else "PC"
      val suffix = (now % 1_000_000L).toString().padStart(6, '0')
      val entry =
        CashbookEntryEntity(
          id = "cb_${UUID.randomUUID().toString().take(8)}",
          code = "$codePrefix-$suffix",
          entryType = if (isIncome) "INCOME" else "EXPENSE",
          title = title.trim(),
          note = note.trim().ifBlank { if (isIncome) "Thu bổ sung sổ quỹ" else "Chi nhập hàng / vận hành" },
          revenueVnd = if (isIncome) amountVnd else 0L,
          costVnd = if (isIncome) 0L else amountVnd,
          profitVnd = if (isIncome) amountVnd else -amountVnd,
          createdAtMillis = now,
        )
      dao.insertCashbookEntry(entry)
      Result.success(Unit)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun createQuickSale(
    lines: List<QuickCartLine>,
    customerName: String,
    customerPhone: String,
    warrantyType: String,
    paymentMethod: String,
  ): Result<ShopOrderEntity> {
    if (lines.isEmpty()) {
      return Result.failure(IllegalArgumentException("Chưa chọn phụ kiện hoặc cường lực"))
    }
    return try {
      val order =
        database.withTransaction {
          val now = System.currentTimeMillis()
          var revenue = 0L
          var cost = 0L
          val summaryParts = mutableListOf<String>()

          for (line in lines) {
            val fresh =
              dao.getProductById(line.product.id)
                ?: throw IllegalStateException("Không tìm thấy ${line.product.name}")
            if (fresh.stock < line.quantity) {
              throw IllegalStateException("${fresh.name} chỉ còn ${fresh.stock} trong kho")
            }
            dao.updateProductStock(fresh.id, fresh.stock - line.quantity, now)
            revenue += fresh.sellPriceVnd * line.quantity
            cost += fresh.costPriceVnd * line.quantity
            summaryParts.add("${fresh.name} x${line.quantity}")
          }

          val profit = revenue - cost
          val customer =
            resolveOrCreateCustomer(
              customerName = customerName,
              customerPhone = customerPhone,
              deviceNote = "",
              addSpentVnd = revenue,
              incrementOrderCount = 1,
            )

          val suffix = (now % 1_000_000L).toString().padStart(6, '0')
          val code = "BH-$suffix"
          val summaryText = summaryParts.joinToString(", ")

          val createdOrder =
            ShopOrderEntity(
              id = "ord_${UUID.randomUUID().toString().take(8)}",
              orderCode = code,
              orderType = "SALE",
              customerId = customer.id,
              customerName = customer.name,
              customerPhone = customer.phone,
              summary = summaryText,
              warrantyType = warrantyType,
              paymentMethod = paymentMethod,
              status = "Hoàn tất",
              costTotalVnd = cost,
              totalVnd = revenue,
              profitVnd = profit,
              createdAtMillis = now,
            )
          dao.insertOrder(createdOrder)

          dao.insertCashbookEntry(
            CashbookEntryEntity(
              id = "cb_${UUID.randomUUID().toString().take(8)}",
              code = code,
              entryType = "SALE",
              title = "Bán nhanh: $summaryText",
              note = "Khách: ${customer.name} · $warrantyType",
              revenueVnd = revenue,
              costVnd = cost,
              profitVnd = profit,
              createdAtMillis = now,
            )
          )
          createdOrder
        }
      Result.success(order)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun createRepairTicket(
    customerName: String,
    customerPhone: String,
    deviceModel: String,
    imei: String,
    issueDescription: String,
    partId: String?,
    manualCostPriceVnd: Long,
    totalPriceVnd: Long,
    warrantyType: String,
  ): Result<RepairTicketEntity> {
    return try {
      require(deviceModel.isNotBlank()) { "Vui lòng nhập dòng máy cần sửa" }
      require(issueDescription.isNotBlank()) { "Vui lòng mô tả lỗi tiếp nhận" }
      require(totalPriceVnd > 0L) { "Giá sửa báo khách phải lớn hơn 0" }

      val ticket =
        database.withTransaction {
          val now = System.currentTimeMillis()
          val part = if (!partId.isNullOrBlank()) dao.getProductById(partId) else null
          if (part != null) {
            if (part.stock <= 0) {
              throw IllegalStateException("Linh kiện ${part.name} đã hết trong kho")
            }
            dao.updateProductStock(part.id, part.stock - 1, now)
          }

          val effectiveCost = part?.costPriceVnd ?: manualCostPriceVnd.coerceAtLeast(0L)
          val profit = totalPriceVnd - effectiveCost
          val customer =
            resolveOrCreateCustomer(
              customerName = customerName,
              customerPhone = customerPhone,
              deviceNote = deviceModel.trim(),
              addSpentVnd = 0L,
              incrementOrderCount = 1,
            )

          val suffix = (now % 1_000_000L).toString().padStart(6, '0')
          val code = "SC-$suffix"
          val cleanPartName = part?.name ?: "Công sửa chữa & kiểm tra"

          val entity =
            RepairTicketEntity(
              id = "rep_${UUID.randomUUID().toString().take(8)}",
              ticketCode = code,
              customerId = customer.id,
              customerName = customer.name,
              customerPhone = customer.phone,
              deviceModel = deviceModel.trim(),
              imei = imei.trim().ifBlank { "Kiểm tra trực tiếp" },
              issueDescription = issueDescription.trim(),
              partId = part?.id,
              partName = cleanPartName,
              costPriceVnd = effectiveCost,
              totalPriceVnd = totalPriceVnd,
              warrantyType = warrantyType,
              status = "Đang nhận",
              createdAtMillis = now,
              updatedAtMillis = now,
            )
          dao.insertRepairTicket(entity)

          val orderSummary =
            if (part != null) "Sửa ${entity.deviceModel} · ${part.name}"
            else "Sửa ${entity.deviceModel} · ${entity.issueDescription}"

          dao.insertOrder(
            ShopOrderEntity(
              id = "ord_${UUID.randomUUID().toString().take(8)}",
              orderCode = code,
              orderType = "REPAIR",
              customerId = customer.id,
              customerName = customer.name,
              customerPhone = customer.phone,
              summary = orderSummary,
              warrantyType = warrantyType,
              paymentMethod = "Thanh toán khi giao",
              status = "Đang nhận",
              costTotalVnd = effectiveCost,
              totalVnd = totalPriceVnd,
              profitVnd = profit,
              createdAtMillis = now,
            )
          )
          entity
        }
      Result.success(ticket)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun updateRepairStatus(
    ticketId: String,
    newStatus: String,
    paymentMethod: String = "Tiền mặt",
  ): Result<Unit> {
    return try {
      database.withTransaction {
        val current =
          dao.getRepairTicketById(ticketId)
            ?: throw IllegalStateException("Không tìm thấy phiếu sửa chữa")
        if (current.status == newStatus) return@withTransaction

        val now = System.currentTimeMillis()
        dao.insertRepairTicket(current.copy(status = newStatus, updatedAtMillis = now))

        val linkedOrder = dao.getOrderByCode(current.ticketCode)
        if (linkedOrder != null) {
          dao.insertOrder(
            linkedOrder.copy(
              status = newStatus,
              paymentMethod = if (newStatus == "Hoàn tất") paymentMethod else linkedOrder.paymentMethod,
            )
          )
        }

        if (newStatus == "Hoàn tất" && current.status != "Hoàn tất") {
          val customer = dao.getCustomerById(current.customerId)
          if (customer != null) {
            dao.insertCustomer(
              customer.copy(totalSpentVnd = customer.totalSpentVnd + current.totalPriceVnd)
            )
          }
          val profit = current.totalPriceVnd - current.costPriceVnd
          dao.insertCashbookEntry(
            CashbookEntryEntity(
              id = "cb_${UUID.randomUUID().toString().take(8)}",
              code = current.ticketCode,
              entryType = "REPAIR",
              title = "Thu sửa ${current.deviceModel} (${current.customerName})",
              note = "${current.partName} · ${current.warrantyType}",
              revenueVnd = current.totalPriceVnd,
              costVnd = current.costPriceVnd,
              profitVnd = profit,
              createdAtMillis = now,
            )
          )
        }
      }
      Result.success(Unit)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun captureSnapshot(sourceApp: String): ShopBackupSnapshot =
    database.withTransaction {
      ShopBackupSnapshot(
        exportedAtMillis = System.currentTimeMillis(),
        sourceApp = sourceApp,
        products = dao.getAllProducts(),
        customers = dao.getAllCustomers(),
        repairTickets = dao.getAllRepairTickets(),
        orders = dao.getAllOrders(),
        cashbookEntries = dao.getAllCashbookEntries(),
      )
    }

  suspend fun exportHqbakJson(sourceApp: String): String =
    withContext(Dispatchers.Default) {
      val snapshot = captureSnapshot(sourceApp)
      ShopBackupCodec.encode(snapshot)
    }

  suspend fun inspectHqbakJson(rawText: String): Result<BackupSummary> =
    withContext(Dispatchers.Default) {
      ShopBackupCodec.decodeAndVerify(rawText).map { it.toSummary() }
    }

  suspend fun restoreFromHqbakJson(rawText: String): Result<BackupSummary> {
    val snapshot =
      withContext(Dispatchers.Default) { ShopBackupCodec.decodeAndVerify(rawText) }
        .getOrElse { return Result.failure(it) }
    return replaceAllWithSnapshot(snapshot)
  }

  suspend fun replaceAllWithSnapshot(snapshot: ShopBackupSnapshot): Result<BackupSummary> {
    return try {
      database.withTransaction {
        dao.clearCashbook()
        dao.clearOrders()
        dao.clearRepairTickets()
        dao.clearCustomers()
        dao.clearProducts()
        dao.insertProducts(snapshot.products)
        dao.insertCustomers(snapshot.customers)
        dao.insertRepairTickets(snapshot.repairTickets)
        dao.insertOrders(snapshot.orders)
        dao.insertCashbookEntries(snapshot.cashbookEntries)
      }
      Result.success(snapshot.toSummary())
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun mergeWithRemoteJson(
    remoteJson: String,
    sourceApp: String,
  ): Result<ShopBackupSnapshot> {
    val remote =
      withContext(Dispatchers.Default) { ShopBackupCodec.decodeAndVerify(remoteJson) }
        .getOrElse { return Result.failure(it) }
    val local = captureSnapshot(sourceApp)
    val merged =
      withContext(Dispatchers.Default) {
        ShopBackupCodec.mergeSnapshots(local = local, remote = remote, sourceApp = sourceApp)
      }
    replaceAllWithSnapshot(merged).getOrElse { return Result.failure(it) }
    return Result.success(merged)
  }

  suspend fun saveLocalQuickBackup(
    context: Context,
    sourceApp: String,
  ): Result<BackupSummary> =
    withContext(Dispatchers.IO) {
      try {
        val snapshot = captureSnapshot(sourceApp)
        val encoded = ShopBackupCodec.encode(snapshot)
        val dir = File(context.applicationContext.filesDir, "hqbak_snapshots")
        if (!dir.exists()) dir.mkdirs()
        val file = File(dir, "latest_dual_backup.hqbak")
        file.writeText(encoded, Charsets.UTF_8)
        Result.success(snapshot.toSummary())
      } catch (e: Exception) {
        Result.failure(e)
      }
    }

  suspend fun getLocalQuickBackupSummary(context: Context): BackupSummary? =
    withContext(Dispatchers.IO) {
      try {
        val file = File(context.applicationContext.filesDir, "hqbak_snapshots/latest_dual_backup.hqbak")
        if (!file.exists()) return@withContext null
        ShopBackupCodec.decodeAndVerify(file.readText(Charsets.UTF_8)).getOrNull()?.toSummary()
      } catch (_: Exception) {
        null
      }
    }

  suspend fun restoreLocalQuickBackup(context: Context): Result<BackupSummary> =
    withContext(Dispatchers.IO) {
      try {
        val file = File(context.applicationContext.filesDir, "hqbak_snapshots/latest_dual_backup.hqbak")
        if (!file.exists()) {
          return@withContext Result.failure(IllegalStateException("Chưa có bản sao lưu trên máy"))
        }
        restoreFromHqbakJson(file.readText(Charsets.UTF_8))
      } catch (e: Exception) {
        Result.failure(e)
      }
    }
}
