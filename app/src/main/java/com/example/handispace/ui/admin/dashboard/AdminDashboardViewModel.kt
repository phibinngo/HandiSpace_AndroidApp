package com.example.handispace.ui.admin.dashboard

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.handispace.data.repository.admin.AdminRepository
import com.example.handispace.model.Product
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MonthlyRevenue(val month: String, val revenue: Double)

data class DashboardState(
    val isLoading: Boolean = true,
    val todaysRevenue: Double = 0.0,
    val totalRevenue: Double = 0.0,
    val totalOrders: Int = 0,
    val totalProducts: Int = 0,
    val totalCategories: Int = 0,
    val totalUsers: Int = 0,
    val revenueChartData: List<MonthlyRevenue> = emptyList(),
    val topProducts: List<Product> = emptyList()
)

@HiltViewModel
class AdminDashboardViewModel @Inject constructor(
    private val adminRepository: AdminRepository
) : ViewModel() {

    var state = mutableStateOf(DashboardState())
        private set

    init {
        loadDashboardData()
        observeTopProducts() // 🔥 GỌI HÀM LẮNG NGHE REAL-TIME LÚC KHỞI TẠO
    }

    // 1. Tải các thống kê tổng quan (Chỉ cần 1 lần lúc mở)
    fun loadDashboardData() {
        viewModelScope.launch {
            state.value = state.value.copy(isLoading = true)

            val realData = adminRepository.getDashboardStats()

            if (realData != null) {
                state.value = state.value.copy(
                    isLoading = false,
                    todaysRevenue = realData.todaysRevenue,
                    totalRevenue = realData.totalRevenue,
                    totalOrders = realData.totalOrders,
                    totalProducts = realData.totalProducts,
                    totalCategories = realData.totalCategories,
                    totalUsers = realData.totalUsers,
                    revenueChartData = realData.revenueChartData
                    // 🔥 LƯU Ý: Không gán topProducts ở đây nữa, nhường cho hàm observeTopProducts lo
                )
            } else {
                state.value = state.value.copy(isLoading = false)
            }
        }
    }

    // 2. 🔥 HÀM LẮNG NGHE TOP BÁN CHẠY REAL-TIME
    private fun observeTopProducts() {
        viewModelScope.launch {
            adminRepository.getTopProductsRealtime().collect { topList ->
                state.value = state.value.copy(topProducts = topList)
            }
        }
    }
}