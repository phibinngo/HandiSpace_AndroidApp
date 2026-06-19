package com.example.handispace.ui.admin.users

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.handispace.data.repository.admin.AdminOrderRepository
import com.example.handispace.data.repository.admin.AdminUserRepository
import com.example.handispace.model.Order
import com.example.handispace.model.User
import com.example.handispace.utils.ResultState
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class AdminUserViewModel @Inject constructor(
    private val userRepo: AdminUserRepository,
    private val orderRepo: AdminOrderRepository,
    private val db: FirebaseFirestore
) : ViewModel() {

    private var allUsers = listOf<User>()

    var usersState = mutableStateOf(ResultState<List<User>>())
        private set

    var searchQuery = mutableStateOf("")

    val successMessage = mutableStateOf<String?>(null)
    val errorMessage = mutableStateOf<String?>(null)

    var specificUserOrders = mutableStateOf<List<Order>>(emptyList())
    var isLoadingOrders = mutableStateOf(false)

    init {
        loadAllUsers()
    }

    fun loadAllUsers() {
        viewModelScope.launch {
            usersState.value = ResultState(isLoading = true)
            val res = userRepo.getAllUsers()

            val safeData = res.data
            if (safeData != null) {
                allUsers = safeData
                applyFilters()
            } else {
                usersState.value = ResultState(errorMessage = res.errorMessage)
            }
        }
    }

    fun applyFilters() {
        val query = searchQuery.value.lowercase().trim()
        var filteredList = allUsers

        if (query.isNotEmpty()) {
            filteredList = filteredList.filter {
                it.name.lowercase().contains(query) ||
                        it.email.lowercase().contains(query) ||
                        it.phone.contains(query)
            }
        }

        usersState.value = ResultState(data = filteredList)
    }

    fun toggleUserLock(user: User) {
        viewModelScope.launch {
            val newStatus = !user.is_disabled

            try {
                // 🔥 ĐÃ FIX LẠI THÀNH "is_disabled" ĐỂ KHỚP VỚI MODEL BÊN USER.KT
                db.collection("users").document(user.uid).update("is_disabled", newStatus).await()

                // Cập nhật trực tiếp trên RAM để UI phản hồi tức thì
                allUsers = allUsers.map {
                    if (it.uid == user.uid) it.copy(is_disabled = newStatus) else it
                }
                applyFilters() // Kích hoạt UI vẽ lại ngay lập tức

                // Báo tin nhắn thành công
                successMessage.value = if (newStatus) "Đã vô hiệu tài khoản thành công!" else "Đã kích hoạt lại tài khoản thành công!"

            } catch (e: Exception) {
                errorMessage.value = "Lỗi khi lưu lên hệ thống: ${e.message}"
            }
        }
    }

    fun clearMessages() {
        successMessage.value = null
        errorMessage.value = null
    }

    fun loadOrdersForUser(userId: String) {
        viewModelScope.launch {
            isLoadingOrders.value = true
            val res = userRepo.getOrdersByUserId(userId)
            specificUserOrders.value = res.data ?: emptyList()
            isLoadingOrders.value = false
        }
    }

    fun updateOrderStatus(orderId: String, newStatus: String, userId: String) {
        viewModelScope.launch {
            orderRepo.updateOrderStatus(orderId, newStatus)
            loadOrdersForUser(userId)
        }
    }
}