package com.example.handispace.ui.user.order.history

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.handispace.data.repository.user.OrderRepository
import com.example.handispace.model.Order
import com.example.handispace.utils.ResultState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OrderHistoryViewModel @Inject constructor(
    private val orderRepository: OrderRepository
) : ViewModel() {

    // 1. Kho lưu trữ CỤC BỘ chứa toàn bộ 19+ đơn hàng kéo từ Firebase về
    private var allOrders = listOf<Order>()

    // 2. State để đẩy data lên giao diện hiển thị (Đã qua bộ lọc)
    var ordersState = mutableStateOf(ResultState<List<Order>>())
        private set

    var searchQuery = mutableStateOf("")
    var selectedStatus = mutableStateOf("all")

    init {
        loadMyOrders()
    }

    fun loadMyOrders() {
        viewModelScope.launch {
            ordersState.value = ResultState(isLoading = true)
            val result = orderRepository.getMyOrders()

            // Hứng data vào biến an toàn
            val safeData = result.data

            if (safeData != null) {
                allOrders = safeData // Lưu cục bộ vào RAM điện thoại
                applyFilters()       // Gọi bộ lọc để xuất data ra UI
            } else {
                ordersState.value = ResultState(errorMessage = result.errorMessage)
            }
        }
    }

    // Hàm này chỉ chạy trên RAM, không gọi Internet nên cực mượt
    fun applyFilters() {
        val query = searchQuery.value.lowercase().trim()
        val status = selectedStatus.value

        // Copy từ kho cục bộ ra để xài
        var filteredList = allOrders

        // Lọc theo Tab trạng thái
        if (status != "all") {
            filteredList = filteredList.filter { it.status == status }
        }

        // Lọc theo ô Tìm kiếm
        if (query.isNotEmpty()) {
            filteredList = filteredList.filter { order ->
                order.order_items.any { item -> item.name.lowercase().contains(query) }
            }
        }

        // Đẩy danh sách đã lọc lên UI
        ordersState.value = ResultState(data = filteredList)
    }

    fun updateSearchQuery(newQuery: String) {
        searchQuery.value = newQuery
        applyFilters()
    }

    fun updateStatusFilter(newStatus: String) {
        selectedStatus.value = newStatus
        applyFilters()
    }
}