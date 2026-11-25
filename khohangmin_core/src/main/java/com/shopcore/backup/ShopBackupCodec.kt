package com.shopcore.backup

import androidx.compose.runtime.Immutable
import com.shopcore.data.CashbookEntryEntity
import com.shopcore.data.CustomerEntity
import com.shopcore.data.ProductEntity
import com.shopcore.data.RepairTicketEntity
import com.shopcore.data.ShopOrderEntity
import org.json.JSONArray
import org.json.JSONObject
import java.nio.charset.StandardCharsets
import java.security.MessageDigest

const val HQBAK_FORMAT = "HQBAK_V1"

@Immutable
data class BackupSummary(
  val exportedAtMillis: Long,
  val sourceApp: String,
  val productsCount: Int,
  val customersCount: Int,
  val repairsCount: Int,
  val ordersCount: Int,
  val cashbookCount: Int,
)

data class ShopBackupSnapshot(
  val exportedAtMillis: Long,
  val sourceApp: String,
  val products: List<ProductEntity>,
  val customers: List<CustomerEntity>,
  val repairTickets: List<RepairTicketEntity>,
  val orders: List<ShopOrderEntity>,
  val cashbookEntries: List<CashbookEntryEntity>,
) {
  fun toSummary(): BackupSummary =
    BackupSummary(
      exportedAtMillis = exportedAtMillis,
      sourceApp = sourceApp,
      productsCount = products.size,
      customersCount = customers.size,
      repairsCount = repairTickets.size,
      ordersCount = orders.size,
      cashbookCount = cashbookEntries.size,
    )
}

object ShopBackupCodec {

  fun encode(snapshot: ShopBackupSnapshot): String {
    val dataObj =
      JSONObject().apply {
        put("products", encodeProducts(snapshot.products))
        put("customers", encodeCustomers(snapshot.customers))
        put("repairTickets", encodeRepairs(snapshot.repairTickets))
        put("orders", encodeOrders(snapshot.orders))
        put("cashbookEntries", encodeCashbook(snapshot.cashbookEntries))
      }
    val canonicalData = dataObj.toString()
    val checksum = sha256Hex(canonicalData)

    return JSONObject()
      .apply {
        put("format", HQBAK_FORMAT)
        put("exportedAtMillis", snapshot.exportedAtMillis)
        put("sourceApp", snapshot.sourceApp)
        put("checksumSha256", checksum)
        put("data", dataObj)
      }
      .toString(2)
  }

  fun decodeAndVerify(rawText: String): Result<ShopBackupSnapshot> {
    return try {
      val trimmed = rawText.trim()
      require(trimmed.isNotEmpty()) { "Tệp .hqbak trống hoặc không đọc được" }
      val root = JSONObject(trimmed)
      val format = root.optString("format", "")
      require(format == HQBAK_FORMAT) {
        "Định dạng tệp không hợp lệ (cần $HQBAK_FORMAT)"
      }

      val dataObj = root.optJSONObject("data")
        ?: throw IllegalArgumentException("Tệp .hqbak thiếu dữ liệu kho và cửa hàng")

      val expectedChecksum = root.optString("checksumSha256", "")
      if (expectedChecksum.isNotBlank()) {
        val actualChecksum = sha256Hex(dataObj.toString())
        require(expectedChecksum.equals(actualChecksum, ignoreCase = true)) {
          "Tệp .hqbak đã bị chỉnh sửa hoặc hư hỏng (sai mã kiểm tra)"
        }
      }

      val snapshot =
        ShopBackupSnapshot(
          exportedAtMillis = root.optLong("exportedAtMillis", System.currentTimeMillis()),
          sourceApp = root.optString("sourceApp", "HQ_KHO_SYNC"),
          products = decodeProducts(dataObj.optJSONArray("products") ?: JSONArray()),
          customers = decodeCustomers(dataObj.optJSONArray("customers") ?: JSONArray()),
          repairTickets = decodeRepairs(dataObj.optJSONArray("repairTickets") ?: JSONArray()),
          orders = decodeOrders(dataObj.optJSONArray("orders") ?: JSONArray()),
          cashbookEntries = decodeCashbook(dataObj.optJSONArray("cashbookEntries") ?: JSONArray()),
        )
      Result.success(snapshot)
    } catch (e: Exception) {
      Result.failure(IllegalArgumentException(e.message ?: "Không thể đọc tệp .hqbak"))
    }
  }

  fun mergeSnapshots(
    local: ShopBackupSnapshot,
    remote: ShopBackupSnapshot,
    sourceApp: String,
  ): ShopBackupSnapshot {
    val mergedProducts =
      (local.products + remote.products)
        .groupBy { it.id }
        .values
        .map { items -> items.maxBy { it.updatedAtMillis } }

    val mergedCustomers =
      (local.customers + remote.customers)
        .groupBy { it.id }
        .values
        .map { items ->
          items.maxWith(
            compareBy<CustomerEntity> { it.orderCount }
              .thenBy { it.totalSpentVnd }
              .thenBy { it.createdAtMillis }
          )
        }

    val mergedRepairs =
      (local.repairTickets + remote.repairTickets)
        .groupBy { it.id }
        .values
        .map { items -> items.maxBy { it.updatedAtMillis } }

    val statusWeight = mapOf("Đang nhận" to 1, "Đang sửa" to 2, "Hoàn tất" to 3)
    val mergedOrders =
      (local.orders + remote.orders)
        .groupBy { it.id }
        .values
        .map { items ->
          items.maxWith(
            compareBy<ShopOrderEntity> { statusWeight[it.status] ?: 0 }
              .thenBy { it.createdAtMillis }
          )
        }

    val mergedCashbook =
      (local.cashbookEntries + remote.cashbookEntries)
        .groupBy { it.id }
        .values
        .map { items -> items.maxBy { it.createdAtMillis } }

    return ShopBackupSnapshot(
      exportedAtMillis = System.currentTimeMillis(),
      sourceApp = sourceApp,
      products = mergedProducts,
      customers = mergedCustomers,
      repairTickets = mergedRepairs,
      orders = mergedOrders,
      cashbookEntries = mergedCashbook,
    )
  }

  private fun sha256Hex(input: String): String {
    val digest = MessageDigest.getInstance("SHA-256")
    val bytes = digest.digest(input.toByteArray(StandardCharsets.UTF_8))
    return bytes.joinToString("") { "%02x".format(it) }
  }

  private fun encodeProducts(items: List<ProductEntity>): JSONArray =
    JSONArray().apply {
      items.forEach { p ->
        put(
          JSONObject().apply {
            put("id", p.id)
            put("sku", p.sku)
            put("name", p.name)
            put("groupName", p.groupName)
            put("subCategory", p.subCategory)
            put("shelfLocation", p.shelfLocation)
            put("costPriceVnd", p.costPriceVnd)
            put("sellPriceVnd", p.sellPriceVnd)
            put("stock", p.stock)
            put("minThreshold", p.minThreshold)
            put("updatedAtMillis", p.updatedAtMillis)
          }
        )
      }
    }

  private fun decodeProducts(arr: JSONArray): List<ProductEntity> =
    List(arr.length()) { idx ->
      val o = arr.getJSONObject(idx)
      ProductEntity(
        id = o.getString("id"),
        sku = o.getString("sku"),
        name = o.getString("name"),
        groupName = o.getString("groupName"),
        subCategory = o.getString("subCategory"),
        shelfLocation = o.getString("shelfLocation"),
        costPriceVnd = o.getLong("costPriceVnd"),
        sellPriceVnd = o.getLong("sellPriceVnd"),
        stock = o.getInt("stock"),
        minThreshold = o.getInt("minThreshold"),
        updatedAtMillis = o.getLong("updatedAtMillis"),
      )
    }

  private fun encodeCustomers(items: List<CustomerEntity>): JSONArray =
    JSONArray().apply {
      items.forEach { c ->
        put(
          JSONObject().apply {
            put("id", c.id)
            put("name", c.name)
            put("phone", c.phone)
            put("deviceNote", c.deviceNote)
            put("totalSpentVnd", c.totalSpentVnd)
            put("orderCount", c.orderCount)
            put("createdAtMillis", c.createdAtMillis)
          }
        )
      }
    }

  private fun decodeCustomers(arr: JSONArray): List<CustomerEntity> =
    List(arr.length()) { idx ->
      val o = arr.getJSONObject(idx)
      CustomerEntity(
        id = o.getString("id"),
        name = o.getString("name"),
        phone = o.getString("phone"),
        deviceNote = o.getString("deviceNote"),
        totalSpentVnd = o.getLong("totalSpentVnd"),
        orderCount = o.getInt("orderCount"),
        createdAtMillis = o.getLong("createdAtMillis"),
      )
    }

  private fun encodeRepairs(items: List<RepairTicketEntity>): JSONArray =
    JSONArray().apply {
      items.forEach { r ->
        put(
          JSONObject().apply {
            put("id", r.id)
            put("ticketCode", r.ticketCode)
            put("customerId", r.customerId)
            put("customerName", r.customerName)
            put("customerPhone", r.customerPhone)
            put("deviceModel", r.deviceModel)
            put("imei", r.imei)
            put("issueDescription", r.issueDescription)
            put("partId", r.partId ?: JSONObject.NULL)
            put("partName", r.partName)
            put("costPriceVnd", r.costPriceVnd)
            put("totalPriceVnd", r.totalPriceVnd)
            put("warrantyType", r.warrantyType)
            put("status", r.status)
            put("createdAtMillis", r.createdAtMillis)
            put("updatedAtMillis", r.updatedAtMillis)
          }
        )
      }
    }

  private fun decodeRepairs(arr: JSONArray): List<RepairTicketEntity> =
    List(arr.length()) { idx ->
      val o = arr.getJSONObject(idx)
      val partId = if (o.isNull("partId")) null else o.optString("partId", "").ifBlank { null }
      RepairTicketEntity(
        id = o.getString("id"),
        ticketCode = o.getString("ticketCode"),
        customerId = o.getString("customerId"),
        customerName = o.getString("customerName"),
        customerPhone = o.getString("customerPhone"),
        deviceModel = o.getString("deviceModel"),
        imei = o.getString("imei"),
        issueDescription = o.getString("issueDescription"),
        partId = partId,
        partName = o.getString("partName"),
        costPriceVnd = o.getLong("costPriceVnd"),
        totalPriceVnd = o.getLong("totalPriceVnd"),
        warrantyType = o.getString("warrantyType"),
        status = o.getString("status"),
        createdAtMillis = o.getLong("createdAtMillis"),
        updatedAtMillis = o.getLong("updatedAtMillis"),
      )
    }

  private fun encodeOrders(items: List<ShopOrderEntity>): JSONArray =
    JSONArray().apply {
      items.forEach { ord ->
        put(
          JSONObject().apply {
            put("id", ord.id)
            put("orderCode", ord.orderCode)
            put("orderType", ord.orderType)
            put("customerId", ord.customerId)
            put("customerName", ord.customerName)
            put("customerPhone", ord.customerPhone)
            put("summary", ord.summary)
            put("warrantyType", ord.warrantyType)
            put("paymentMethod", ord.paymentMethod)
            put("status", ord.status)
            put("costTotalVnd", ord.costTotalVnd)
            put("totalVnd", ord.totalVnd)
            put("profitVnd", ord.profitVnd)
            put("createdAtMillis", ord.createdAtMillis)
          }
        )
      }
    }

  private fun decodeOrders(arr: JSONArray): List<ShopOrderEntity> =
    List(arr.length()) { idx ->
      val o = arr.getJSONObject(idx)
      ShopOrderEntity(
        id = o.getString("id"),
        orderCode = o.getString("orderCode"),
        orderType = o.getString("orderType"),
        customerId = o.getString("customerId"),
        customerName = o.getString("customerName"),
        customerPhone = o.getString("customerPhone"),
        summary = o.getString("summary"),
        warrantyType = o.getString("warrantyType"),
        paymentMethod = o.getString("paymentMethod"),
        status = o.getString("status"),
        costTotalVnd = o.getLong("costTotalVnd"),
        totalVnd = o.getLong("totalVnd"),
        profitVnd = o.getLong("profitVnd"),
        createdAtMillis = o.getLong("createdAtMillis"),
      )
    }

  private fun encodeCashbook(items: List<CashbookEntryEntity>): JSONArray =
    JSONArray().apply {
      items.forEach { cb ->
        put(
          JSONObject().apply {
            put("id", cb.id)
            put("code", cb.code)
            put("entryType", cb.entryType)
            put("title", cb.title)
            put("note", cb.note)
            put("revenueVnd", cb.revenueVnd)
            put("costVnd", cb.costVnd)
            put("profitVnd", cb.profitVnd)
            put("createdAtMillis", cb.createdAtMillis)
          }
        )
      }
    }

  private fun decodeCashbook(arr: JSONArray): List<CashbookEntryEntity> =
    List(arr.length()) { idx ->
      val o = arr.getJSONObject(idx)
      CashbookEntryEntity(
        id = o.getString("id"),
        code = o.getString("code"),
        entryType = o.getString("entryType"),
        title = o.getString("title"),
        note = o.getString("note"),
        revenueVnd = o.getLong("revenueVnd"),
        costVnd = o.getLong("costVnd"),
        profitVnd = o.getLong("profitVnd"),
        createdAtMillis = o.getLong("createdAtMillis"),
      )
    }
}
