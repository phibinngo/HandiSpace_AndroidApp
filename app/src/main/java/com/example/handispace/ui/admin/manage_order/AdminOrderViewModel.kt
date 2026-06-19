package com.example.handispace.ui.admin.orders

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.handispace.data.repository.admin.AdminOrderRepository
import com.example.handispace.model.Order
import com.example.handispace.utils.ResultState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminOrderViewModel @Inject constructor(
    private val adminOrderRepository: AdminOrderRepository
) : ViewModel() {

    private var allOrders = listOf<Order>()

    var ordersState = mutableStateOf(ResultState<List<Order>>())
        private set

    var selectedTab = mutableStateOf("Tất cả")
    var searchQuery = mutableStateOf("")
    var isLoading = mutableStateOf(false)

    var pendingCount = mutableStateOf(0)
    var returnCount = mutableStateOf(0)

    val tabs = listOf("Tất cả", "Chờ xác nhận", "Đang xử lý", "Đang giao", "Hoàn thành", "Yêu cầu trả", "Đã Hủy/Trả")

    init {
        listenToOrdersRealtime()
    }

    private fun listenToOrdersRealtime() {
        viewModelScope.launch {
            isLoading.value = true
            adminOrderRepository.getAllOrdersRealtime().collectLatest { orders ->
                allOrders = orders

                pendingCount.value = orders.count { it.status == "pending" }
                returnCount.value = orders.count { it.status == "return_pending" }

                applyFilters()
                isLoading.value = false
            }
        }
    }

    fun applyFilters() {
        val query = searchQuery.value.lowercase().trim()
        var filteredList = allOrders

        if (selectedTab.value != "Tất cả") {
            val statusFilter = when (selectedTab.value) {
                "Chờ xác nhận" -> "pending"
                "Đang xử lý" -> "preparing"
                "Đang giao" -> "delivering"
                "Hoàn thành" -> "completed"
                "Yêu cầu trả" -> "return_pending"
                "Đã Hủy/Trả" -> listOf("cancelled", "returned", "refunded")
                else -> ""
            }
            filteredList = if (statusFilter is List<*>) {
                filteredList.filter { it.status in statusFilter }
            } else {
                filteredList.filter { it.status == statusFilter }
            }
        }

        if (query.isNotEmpty()) {
            filteredList = filteredList.filter { order ->
                order.order_id.lowercase().contains(query) ||
                        order.shipping_info.phone.contains(query) ||
                        order.shipping_info.name.lowercase().contains(query)
            }
        }

        ordersState.value = ResultState(data = filteredList)
    }

    fun updateStatusFilter(tabName: String) {
        selectedTab.value = tabName
        applyFilters()
    }

    fun updateSearchQuery(newQuery: String) {
        searchQuery.value = newQuery
        applyFilters()
    }

    fun getFilteredOrders(): List<Order> {
        return ordersState.value.data ?: emptyList()
    }

    fun updateStatus(orderId: String, newStatus: String) {
        viewModelScope.launch {
            adminOrderRepository.updateOrderStatus(orderId, newStatus)
        }
    }
}