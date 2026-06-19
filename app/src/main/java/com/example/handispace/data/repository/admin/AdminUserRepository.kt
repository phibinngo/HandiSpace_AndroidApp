package com.example.handispace.data.repository.admin

import com.example.handispace.model.Order
import com.example.handispace.model.User
import com.example.handispace.utils.ResultState
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AdminUserRepository @Inject constructor(
    private val db: FirebaseFirestore
) {
    suspend fun getAllUsers(): ResultState<List<User>> {
        return try {
            val snap = db.collection("users")
                .whereEqualTo("role", "customer")
                .get().await()

            val users = snap.documents.mapNotNull { it.toObject(User::class.java) }

            val sortedUsers = users.sortedByDescending { it.created_at?.seconds ?: 0L }
            ResultState(data = sortedUsers)
        } catch (e: Exception) {
            ResultState(errorMessage = e.message ?: "Lỗi tải danh sách người dùng")
        }
    }

    suspend fun getOrdersByUserId(userId: String): ResultState<List<Order>> {
        return try {
            val snap = db.collection("orders")
                .whereEqualTo("user_id", userId)
                .get().await()

            val orders = snap.documents.mapNotNull { it.toObject(Order::class.java) }

            val sortedOrders = orders.sortedByDescending { it.created_at?.seconds ?: 0L }

            ResultState(data = sortedOrders)
        } catch (e: Exception) {
            ResultState(errorMessage = e.message ?: "Lỗi lấy đơn hàng của User")
        }
    }

    suspend fun toggleUserStatus(userId: String, isDisabled: Boolean): ResultState<Boolean> {
        return try {
            db.collection("users").document(userId).update("is_disabled", isDisabled).await()
            ResultState(data = true)
        } catch (e: Exception) {
            ResultState(errorMessage = e.message ?: "Lỗi cập nhật trạng thái User")
        }
    }
}