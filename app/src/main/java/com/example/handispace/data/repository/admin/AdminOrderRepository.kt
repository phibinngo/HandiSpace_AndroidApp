package com.example.handispace.data.repository.admin

import com.example.handispace.model.LoyaltyLevel
import com.example.handispace.model.Order
import com.example.handispace.utils.ResultState
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AdminOrderRepository @Inject constructor(
    private val db: FirebaseFirestore
) {
    fun getAllOrdersRealtime(): Flow<List<Order>> = callbackFlow {
        val listener = db.collection("orders")
            .orderBy("created_at", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val orders = snapshot?.documents?.mapNotNull { it.toObject(Order::class.java) } ?: emptyList()
                trySend(orders)
            }
        awaitClose { listener.remove() }
    }

    // 🔥 HÀM TÍNH RANK (Được copy sang đây để Admin cũng tự tính được)
    private fun calculateLoyaltyLevel(totalSpent: Double): LoyaltyLevel {
        return when {
            totalSpent >= 10_000_000 -> LoyaltyLevel("Kim Cương", 8.0)
            totalSpent >= 5_000_000 -> LoyaltyLevel("Vàng", 5.0)
            totalSpent >= 2_000_000 -> LoyaltyLevel("Bạc", 2.0)
            else -> LoyaltyLevel("Thành viên", 0.0)
        }
    }

    suspend fun updateOrderStatus(orderId: String, newStatus: String): ResultState<Boolean> {
        return try {
            // 🔥 BƯỚC 1: LẤY DATA CŨ TRƯỚC KHI UPDATE ĐỂ BIẾT HOÀN CẢNH
            val orderDoc = db.collection("orders").document(orderId).get().await()
            val uId = orderDoc.getString("user_id") ?: ""
            val code = orderId.takeLast(6).uppercase()
            val oldStatus = orderDoc.getString("status") ?: ""

            // 🔥 BƯỚC 2: CẬP NHẬT TRẠNG THÁI MỚI LÊN FIREBASE
            db.collection("orders").document(orderId).update("status", newStatus).await()

            // ==========================================
            // 🔥 BƯỚC ĐỘT PHÁ: TỰ ĐỘNG CỘNG/TRỪ TIỀN VÀ RANK CHO USER
            // ==========================================
            if (uId.isNotBlank()) {
                val subtotal = orderDoc.getDouble("subtotal") ?: 0.0
                val orderDiscount = orderDoc.getDouble("order_discount") ?: 0.0
                val memberDiscount = orderDoc.getDouble("member_discount") ?: 0.0
                val eligibleSpentAmount = (subtotal - orderDiscount - memberDiscount).coerceAtLeast(0.0)

                val userDoc = db.collection("users").document(uId).get().await()
                val currentTotalSpent = userDoc.getDouble("total_spent") ?: 0.0

                // KỊCH BẢN 1: ĐƠN CHUYỂN THÀNH COMPLETED -> CỘNG TIỀN VÀ THĂNG HẠNG
                if (newStatus == "completed" && oldStatus != "completed") {
                    val newTotalSpent = currentTotalSpent + eligibleSpentAmount
                    val newRank = calculateLoyaltyLevel(newTotalSpent)
                    db.collection("users").document(uId).update(
                        "total_spent", newTotalSpent,
                        "loyalty_level", newRank
                    ).await()
                }

                // KỊCH BẢN 2: LỠ TAY TỪ COMPLETED CHUYỂN SANG HỦY/TRẢ HÀNG -> TRỪ TIỀN LẠI VÀ GIÁNG HẠNG
                if (oldStatus == "completed" && (newStatus == "returned" || newStatus == "cancelled")) {
                    val newTotalSpent = (currentTotalSpent - eligibleSpentAmount).coerceAtLeast(0.0)
                    val newRank = calculateLoyaltyLevel(newTotalSpent)
                    db.collection("users").document(uId).update(
                        "total_spent", newTotalSpent,
                        "loyalty_level", newRank
                    ).await()
                }
            }
            // ==========================================


            // 🔥 BƯỚC 3: CHIA NHÁNH THÔNG BÁO THÔNG MINH
            val (title, msg) = when(newStatus) {
                "preparing" -> "Đơn hàng đang chuẩn bị" to "Người bán đang đóng gói kiện hàng #$code của bạn."
                "delivering" -> "Đơn hàng đang giao" to "Kiện hàng #$code đã được bàn giao cho shipper và đang trên đường đến bạn."

                "completed" -> {
                    // Tách biệt rạch ròi 2 trường hợp của completed
                    if (oldStatus == "return_pending") {
                        "Từ chối trả hàng" to "Yêu cầu trả hàng cho đơn #$code của bạn đã bị từ chối. Đơn hàng được chuyển về trạng thái Hoàn tất."
                    } else {
                        "Giao hàng thành công" to "Đơn hàng #$code đã được giao đến bạn. Cảm ơn bạn đã mua sắm!"
                    }
                }

                "cancelled" -> "Đơn hàng đã hủy" to "Đơn hàng #$code đã bị hủy bởi hệ thống."
                "returned" -> "Hoàn tiền thành công" to "Yêu cầu trả hàng được chấp nhận. Đơn #$code sẽ được hoàn tiền."
                else -> "Cập nhật đơn hàng" to "Đơn hàng #$code của bạn vừa thay đổi trạng thái."
            }

            // Bắn thông báo về cho User
            if (uId.isNotBlank()) {
                val notifId = db.collection("notifications").document().id
                val notificationData = mapOf(
                    "id" to notifId,
                    "user_id" to uId,
                    "title" to title,
                    "message" to msg,
                    "type" to "order",
                    "is_read" to false,
                    "created_at" to Timestamp.now()
                )
                db.collection("notifications").document(notifId).set(notificationData).await()
            }

            ResultState(data = true)
        } catch (e: Exception) {
            ResultState(errorMessage = e.message ?: "Lỗi khi cập nhật trạng thái đơn hàng.")
        }
    }
}