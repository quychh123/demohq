package com.example.ui

import android.content.Context
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.shopcore.data.CustomerEntity
import com.shopcore.data.ProductEntity
import com.shopcore.data.QuickCartLine
import com.shopcore.data.RepairTicketEntity
import com.shopcore.data.ShopOrderEntity
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
data class HqAppleUiState(
  val products: List<ProductEntity> = emptyList(),
  val customers: List<CustomerEntity> = emptyList(),
  val repairTickets: List<RepairTicketEntity> = emptyList(),
  val filteredRepairs: List<RepairTicketEntity> = emptyList(),
  val filteredQuickSaleProducts: List<ProductEntity> = emptyList(),
  val orders: List<ShopOrderEntity> = emptyList(),
  val filteredOrders: List<ShopOrderEntity> = emptyList(),
  val cashbookCount: Int = 0,
  val cartLines: List<QuickCartLine> = emptyList(),
  val cartItemCount: Int = 0,
  val cartTotalVnd: Long = 0L,
  val totalOrderRevenueVnd: Long = 0L,
  val totalOrderProfitVnd: Long = 0L,
  val repairSearchQuery: String = "",
  val repairStatusFilter: String = "Tất cả",
  val quickSaleSearchQuery: String = "",
  val quickSaleCategory: String = "Tất cả",
  val cartQuantities: Map<String, Int> = emptyMap(),
  val orderSearchQuery: String = "",
  val orderTypeFilter: String = "Tất cả",
  val darkTheme: Boolean = false,
  val message: String? = null,
)

class HqAppleViewModel(
  appContext: Context,
  private val repository: ShopRepository,
) : ViewModel() {

  private val repairSearch = MutableStateFlow("")
  private val repairStatusFilter = MutableStateFlow("Tất cả")
  private val quickSaleSearch = MutableStateFlow("")
  private val quickSaleCategory = MutableStateFlow("Tất cả")
  private val cartQuantities = MutableStateFlow<Map<String, Int>>(emptyMap())
  private val orderSearch = MutableStateFlow("")
  private val orderTypeFilter = MutableStateFlow("Tất cả")
  private val darkTheme = MutableStateFlow(false)
  private val message = MutableStateFlow<String?>(null)

  val syncManager =
    ShopSyncManager(
      context = appContext,
      repository = repository,
      clientAppName = "HQ Apple",
      scope = viewModelScope,
      onNotify = { msg -> message.value = msg },
    )

  val syncUiState: StateFlow<ShopSyncUiState> = syncManager.uiState

  private val dataFlow =
    combine(
      repository.products,
      repository.customers,
      repository.repairTickets,
      repository.orders,
      repository.cashbookEntries,
    ) { products, customers, repairs, orders, cashbook ->
      DataBundle(products, customers, repairs, orders, cashbook.size)
    }

  private val filterFlow =
    combine(
      repairSearch,
      repairStatusFilter,
      quickSaleSearch,
      quickSaleCategory,
      cartQuantities,
    ) { rSearch, rStatus, qSearch, qCat, cart ->
      PosBundle(rSearch, rStatus, qSearch, qCat, cart)
    }

  private val orderUiFlow =
    combine(orderSearch, orderTypeFilter, darkTheme, message) { oSearch, oType, dark, msg ->
      OrderUiBundle(oSearch, oType, dark, msg)
    }

  val uiState: StateFlow<HqAppleUiState> =
    combine(dataFlow, filterFlow, orderUiFlow) { data, pos, oUi ->
      val rQuery = pos.repairSearch.trim().lowercase()
      val filteredRepairs =
        data.repairs.filter { ticket ->
          val matchesStatus =
            pos.repairStatusFilter == "Tất cả" || ticket.status == pos.repairStatusFilter
          val matchesQuery =
            rQuery.isEmpty() ||
              ticket.ticketCode.lowercase().contains(rQuery) ||
              ticket.customerName.lowercase().contains(rQuery) ||
              ticket.customerPhone.lowercase().contains(rQuery) ||
              ticket.deviceModel.lowercase().contains(rQuery) ||
              ticket.imei.lowercase().contains(rQuery) ||
              ticket.issueDescription.lowercase().contains(rQuery)
          matchesStatus && matchesQuery
        }

      val qQuery = pos.quickSaleSearch.trim().lowercase()
      val filteredQuickSale =
        data.products.filter { item ->
          val matchesCategory =
            pos.quickSaleCategory == "Tất cả" || item.subCategory == pos.quickSaleCategory
          val matchesQuery =
            qQuery.isEmpty() ||
              item.name.lowercase().contains(qQuery) ||
              item.sku.lowercase().contains(qQuery) ||
              item.subCategory.lowercase().contains(qQuery)
          matchesCategory && matchesQuery
        }

      val cartLines =
        pos.cart.mapNotNull { (productId, qty) ->
          val product = data.products.find { it.id == productId }
          if (product != null && qty > 0) QuickCartLine(product, qty) else null
        }

      val oQuery = oUi.orderSearch.trim().lowercase()
      val filteredOrders =
        data.orders.filter { order ->
          val matchesType =
            when (oUi.orderTypeFilter) {
              "Bán ra" -> order.orderType == "SALE"
              "Sửa chữa" -> order.orderType == "REPAIR"
              else -> true
            }
          val matchesQuery =
            oQuery.isEmpty() ||
              order.orderCode.lowercase().contains(oQuery) ||
              order.customerName.lowercase().contains(oQuery) ||
              order.customerPhone.lowercase().contains(oQuery) ||
              order.summary.lowercase().contains(oQuery)
          matchesType && matchesQuery
        }

      val completedOrders = data.orders.filter { it.status == "Hoàn tất" }

      HqAppleUiState(
        products = data.products,
        customers = data.customers,
        repairTickets = data.repairs,
        filteredRepairs = filteredRepairs,
        filteredQuickSaleProducts = filteredQuickSale,
        orders = data.orders,
        filteredOrders = filteredOrders,
        cashbookCount = data.cashbookCount,
        cartLines = cartLines,
        cartItemCount = cartLines.sumOf { it.quantity },
        cartTotalVnd = cartLines.sumOf { it.product.sellPriceVnd * it.quantity },
        totalOrderRevenueVnd = completedOrders.sumOf { it.totalVnd },
        totalOrderProfitVnd = completedOrders.sumOf { it.profitVnd },
        repairSearchQuery = pos.repairSearch,
        repairStatusFilter = pos.repairStatusFilter,
        quickSaleSearchQuery = pos.quickSaleSearch,
        quickSaleCategory = pos.quickSaleCategory,
        cartQuantities = pos.cart,
        orderSearchQuery = oUi.orderSearch,
        orderTypeFilter = oUi.orderTypeFilter,
        darkTheme = oUi.darkTheme,
        message = oUi.message,
      )
    }
      .flowOn(Dispatchers.Default)
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = HqAppleUiState(),
      )

  init {
    viewModelScope.launch { repository.purgeLegacySampleDataOnce(appContext) }
  }

  fun setRepairSearch(value: String) {
    repairSearch.value = value
  }

  fun setRepairStatusFilter(value: String) {
    repairStatusFilter.value = value
  }

  fun setQuickSaleSearch(value: String) {
    quickSaleSearch.value = value
  }

  fun setQuickSaleCategory(value: String) {
    quickSaleCategory.value = value
  }

  fun setOrderSearch(value: String) {
    orderSearch.value = value
  }

  fun setOrderTypeFilter(value: String) {
    orderTypeFilter.value = value
  }

  fun toggleDarkTheme() {
    darkTheme.value = !darkTheme.value
  }

  fun clearMessage() {
    message.value = null
  }

  fun addToCart(product: ProductEntity) {
    val current = cartQuantities.value[product.id] ?: 0
    if (product.stock <= 0) {
      message.value = "${product.name} đang hết hàng trong kho"
      return
    }
    if (current + 1 > product.stock) {
      message.value = "${product.name} chỉ còn ${product.stock} trong kho"
      return
    }
    cartQuantities.value = cartQuantities.value + (product.id to (current + 1))
  }

  fun decrementCart(productId: String) {
    val current = cartQuantities.value[productId] ?: 0
    cartQuantities.value =
      if (current <= 1) cartQuantities.value - productId
      else cartQuantities.value + (productId to (current - 1))
  }

  fun clearCart() {
    cartQuantities.value = emptyMap()
  }

  fun checkoutQuickSale(
    customerName: String,
    customerPhone: String,
    warrantyType: String,
    paymentMethod: String,
    onCompleted: (ShopOrderEntity) -> Unit,
  ) {
    val lines = uiState.value.cartLines
    viewModelScope.launch {
      val result =
        repository.createQuickSale(
          lines = lines,
          customerName = customerName,
          customerPhone = customerPhone,
          warrantyType = warrantyType,
          paymentMethod = paymentMethod,
        )
      result
        .onSuccess { order ->
          cartQuantities.value = emptyMap()
          message.value = "Đã thanh toán đơn ${order.orderCode} & đồng bộ kho"
          onCompleted(order)
          syncManager.triggerAutoSyncIfEnabled()
        }
        .onFailure { err -> message.value = err.message ?: "Không thể tạo đơn bán nhanh" }
    }
  }

  fun createRepairTicket(
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
  ) {
    viewModelScope.launch {
      val result =
        repository.createRepairTicket(
          customerName = customerName,
          customerPhone = customerPhone,
          deviceModel = deviceModel,
          imei = imei,
          issueDescription = issueDescription,
          partId = partId,
          manualCostPriceVnd = manualCostPriceVnd,
          totalPriceVnd = totalPriceVnd,
          warrantyType = warrantyType,
        )
      result
        .onSuccess { ticket ->
          message.value = "Đã lập phiếu tiếp nhận ${ticket.ticketCode}"
          onSuccess(ticket)
          syncManager.triggerAutoSyncIfEnabled()
        }
        .onFailure { err -> message.value = err.message ?: "Không thể lập phiếu sửa chữa" }
    }
  }

  fun updateRepairStatus(
    ticketId: String,
    newStatus: String,
    paymentMethod: String = "Tiền mặt",
  ) {
    viewModelScope.launch {
      val result =
        repository.updateRepairStatus(
          ticketId = ticketId,
          newStatus = newStatus,
          paymentMethod = paymentMethod,
        )
      result
        .onSuccess {
          message.value =
            if (newStatus == "Hoàn tất") "Đã hoàn tất sửa chữa & ghi Sổ quỹ"
            else "Đã chuyển trạng thái phiếu sang $newStatus"
          syncManager.triggerAutoSyncIfEnabled()
        }
        .onFailure { err -> message.value = err.message ?: "Không thể cập nhật trạng thái" }
    }
  }

  fun clearAllData() {
    viewModelScope.launch {
      repository.clearAllData()
      cartQuantities.value = emptyMap()
      message.value = "Đã xóa toàn bộ dữ liệu trên máy"
    }
  }

  private data class DataBundle(
    val products: List<ProductEntity>,
    val customers: List<CustomerEntity>,
    val repairs: List<RepairTicketEntity>,
    val orders: List<ShopOrderEntity>,
    val cashbookCount: Int,
  )

  private data class PosBundle(
    val repairSearch: String,
    val repairStatusFilter: String,
    val quickSaleSearch: String,
    val quickSaleCategory: String,
    val cart: Map<String, Int>,
  )

  private data class OrderUiBundle(
    val orderSearch: String,
    val orderTypeFilter: String,
    val darkTheme: Boolean,
    val message: String?,
  )

  class Factory(
    private val appContext: Context,
    private val repository: ShopRepository,
  ) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      return HqAppleViewModel(appContext, repository) as T
    }
  }
}
