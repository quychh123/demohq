package com.khohangmin.ui

import android.content.Context
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.shopcore.data.CashbookEntryEntity
import com.shopcore.data.CustomerEntity
import com.shopcore.data.ProductEntity
import com.shopcore.data.ShopRepository
import com.shopcore.sync.ShopSyncManager
import com.shopcore.sync.ShopSyncUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@Immutable
data class WarehouseUiState(
  val products: List<ProductEntity> = emptyList(),
  val filteredProducts: List<ProductEntity> = emptyList(),
  val lowStockItems: List<ProductEntity> = emptyList(),
  val customers: List<CustomerEntity> = emptyList(),
  val filteredCustomers: List<CustomerEntity> = emptyList(),
  val cashbookEntries: List<CashbookEntryEntity> = emptyList(),
  val filteredCashbook: List<CashbookEntryEntity> = emptyList(),
  val repairsCount: Int = 0,
  val ordersCount: Int = 0,
  val totalSkus: Int = 0,
  val totalUnits: Int = 0,
  val inventoryCostVnd: Long = 0L,
  val totalRevenueVnd: Long = 0L,
  val totalCostVnd: Long = 0L,
  val totalProfitVnd: Long = 0L,
  val goodsSearchQuery: String = "",
  val goodsFilter: String = "Tất cả",
  val cashbookFilter: String = "Tất cả",
  val customerSearchQuery: String = "",
  val darkTheme: Boolean = false,
  val message: String? = null,
)

class WarehouseViewModel(
  appContext: Context,
  private val repository: ShopRepository,
) : ViewModel() {

  private val goodsSearch = MutableStateFlow("")
  private val goodsFilter = MutableStateFlow("Tất cả")
  private val cashbookFilter = MutableStateFlow("Tất cả")
  private val customerSearch = MutableStateFlow("")
  private val darkTheme = MutableStateFlow(false)
  private val message = MutableStateFlow<String?>(null)

  val syncManager =
    ShopSyncManager(
      context = appContext,
      repository = repository,
      clientAppName = "Kho Hàng Min",
      scope = viewModelScope,
      onNotify = { msg -> message.value = msg },
    )

  val syncUiState: StateFlow<ShopSyncUiState> = syncManager.uiState

  private val dataFlow =
    combine(
      repository.products,
      repository.customers,
      repository.cashbookEntries,
      repository.repairTickets,
      repository.orders,
    ) { products, customers, cashbook, repairs, orders ->
      DataBundle(products, customers, cashbook, repairs.size, orders.size)
    }

  private val filterFlow =
    combine(
      goodsSearch,
      goodsFilter,
      cashbookFilter,
      customerSearch,
    ) { gSearch, gFilter, cFilter, custSearch ->
      FilterBundle(gSearch, gFilter, cFilter, custSearch)
    }

  val uiState: StateFlow<WarehouseUiState> =
    combine(dataFlow, filterFlow, darkTheme, message) { data, filters, isDark, msg ->
      val products = data.products
      val customers = data.customers
      val cashbook = data.cashbook

      val gQuery = filters.goodsSearch.trim().lowercase()
      val filteredProducts =
        products.filter { item ->
          val matchesFilter =
            when (filters.goodsFilter) {
              "Tất cả" -> true
              "Linh kiện" -> item.groupName == "Linh kiện"
              "Sắp hết" -> item.stock <= item.minThreshold
              else -> item.subCategory == filters.goodsFilter
            }
          val matchesQuery =
            gQuery.isEmpty() ||
              item.name.lowercase().contains(gQuery) ||
              item.sku.lowercase().contains(gQuery) ||
              item.shelfLocation.lowercase().contains(gQuery) ||
              item.subCategory.lowercase().contains(gQuery)
          matchesFilter && matchesQuery
        }

      val filteredCashbook =
        cashbook.filter { entry ->
          when (filters.cashbookFilter) {
            "Bán hàng" -> entry.entryType == "SALE"
            "Sửa chữa" -> entry.entryType == "REPAIR"
            "Thu khác" -> entry.entryType == "INCOME"
            "Chi phí" -> entry.entryType == "EXPENSE"
            else -> true
          }
        }

      val cQuery = filters.customerSearch.trim().lowercase()
      val filteredCustomers =
        if (cQuery.isEmpty()) customers
        else {
          customers.filter { customer ->
            customer.name.lowercase().contains(cQuery) ||
              customer.phone.lowercase().contains(cQuery) ||
              customer.deviceNote.lowercase().contains(cQuery)
          }
        }

      WarehouseUiState(
        products = products,
        filteredProducts = filteredProducts,
        lowStockItems = products.filter { it.stock <= it.minThreshold }.sortedBy { it.stock },
        customers = customers,
        filteredCustomers = filteredCustomers,
        cashbookEntries = cashbook,
        filteredCashbook = filteredCashbook,
        repairsCount = data.repairsCount,
        ordersCount = data.ordersCount,
        totalSkus = products.size,
        totalUnits = products.sumOf { it.stock },
        inventoryCostVnd = products.sumOf { it.costPriceVnd * it.stock },
        totalRevenueVnd = cashbook.sumOf { it.revenueVnd },
        totalCostVnd = cashbook.sumOf { it.costVnd },
        totalProfitVnd = cashbook.sumOf { it.profitVnd },
        goodsSearchQuery = filters.goodsSearch,
        goodsFilter = filters.goodsFilter,
        cashbookFilter = filters.cashbookFilter,
        customerSearchQuery = filters.customerSearch,
        darkTheme = isDark,
        message = msg,
      )
    }
      .flowOn(Dispatchers.Default)
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = WarehouseUiState(),
      )

  init {
    viewModelScope.launch { repository.purgeLegacySampleDataOnce(appContext) }
  }

  fun setGoodsSearch(value: String) {
    goodsSearch.value = value
  }

  fun setGoodsFilter(value: String) {
    goodsFilter.value = value
  }

  fun setCashbookFilter(value: String) {
    cashbookFilter.value = value
  }

  fun setCustomerSearch(value: String) {
    customerSearch.value = value
  }

  fun toggleDarkTheme() {
    darkTheme.value = !darkTheme.value
  }

  fun clearMessage() {
    message.value = null
  }

  fun adjustStock(productId: String, delta: Int) {
    viewModelScope.launch {
      repository.adjustProductStock(productId, delta)
      message.value =
        if (delta > 0) "Đã nhập thêm +$delta sản phẩm vào kho" else "Đã xuất bớt ${-delta} sản phẩm"
      syncManager.triggerAutoSyncIfEnabled()
    }
  }

  fun saveProduct(
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
  ) {
    viewModelScope.launch {
      val result =
        repository.saveProduct(
          existingId = existingId,
          sku = sku,
          name = name,
          subCategory = subCategory,
          shelfLocation = shelfLocation,
          costPriceVnd = costPriceVnd,
          sellPriceVnd = sellPriceVnd,
          stock = stock,
          minThreshold = minThreshold,
        )
      result
        .onSuccess {
          message.value =
            if (existingId == null) "Đã thêm hàng hóa mới" else "Đã cập nhật hàng hóa"
          onSuccess()
          syncManager.triggerAutoSyncIfEnabled()
        }
        .onFailure { err -> message.value = err.message ?: "Không thể lưu hàng hóa" }
    }
  }

  fun deleteProduct(product: ProductEntity) {
    viewModelScope.launch {
      repository.deleteProduct(product)
      message.value = "Đã xóa ${product.name}"
      syncManager.triggerAutoSyncIfEnabled()
    }
  }

  fun addManualCashbookEntry(
    isIncome: Boolean,
    title: String,
    amountVnd: Long,
    note: String,
    onSuccess: () -> Unit,
  ) {
    viewModelScope.launch {
      val result =
        repository.addManualCashbookEntry(
          isIncome = isIncome,
          title = title,
          amountVnd = amountVnd,
          note = note,
        )
      result
        .onSuccess {
          message.value =
            if (isIncome) "Đã ghi phiếu thu vào sổ quỹ" else "Đã ghi phiếu chi vào sổ quỹ"
          onSuccess()
          syncManager.triggerAutoSyncIfEnabled()
        }
        .onFailure { err -> message.value = err.message ?: "Không thể tạo phiếu quỹ" }
    }
  }

  fun saveCustomer(
    existingId: String?,
    name: String,
    phone: String,
    deviceNote: String,
    onSuccess: () -> Unit,
  ) {
    viewModelScope.launch {
      val result =
        repository.saveCustomer(
          existingId = existingId,
          name = name,
          phone = phone,
          deviceNote = deviceNote,
        )
      result
        .onSuccess {
          message.value =
            if (existingId == null) "Đã thêm khách hàng mới" else "Đã cập nhật khách hàng"
          onSuccess()
          syncManager.triggerAutoSyncIfEnabled()
        }
        .onFailure { err -> message.value = err.message ?: "Không thể lưu khách hàng" }
    }
  }

  fun deleteCustomer(customer: CustomerEntity) {
    viewModelScope.launch {
      repository.deleteCustomer(customer)
      message.value = "Đã xóa khách hàng ${customer.name}"
      syncManager.triggerAutoSyncIfEnabled()
    }
  }

  fun clearAllData() {
    viewModelScope.launch {
      repository.clearAllData()
      message.value = "Đã xóa toàn bộ dữ liệu trên máy"
    }
  }

  private data class DataBundle(
    val products: List<ProductEntity>,
    val customers: List<CustomerEntity>,
    val cashbook: List<CashbookEntryEntity>,
    val repairsCount: Int,
    val ordersCount: Int,
  )

  private data class FilterBundle(
    val goodsSearch: String,
    val goodsFilter: String,
    val cashbookFilter: String,
    val customerSearch: String,
  )

  class Factory(
    private val appContext: Context,
    private val repository: ShopRepository,
  ) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      return WarehouseViewModel(appContext, repository) as T
    }
  }
}
