package com.example.handispace.data.repository.user

import com.example.handispace.model.Notification
import com.example.handispace.model.Order
import com.example.handispace.model.OrderItem
import com.example.handispace.model.ShippingInfo
import com.example.handispace.utils.ResultState
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import kotlin.collections.get

class OrderRepository @Inject constructor(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    suspend fun getMyOrders(): ResultState<List<Order>> {
        return try {
            val userId = auth.currentUser?.uid ?: return ResultState(errorMessage = "Vui lòng đăng nhập!")
            val snapshot = db.collection("orders").whereEqualTo("user_id", userId).get().await()
            val orderList = mutableListOf<Order>()

            for (doc in snapshot.documents) {
                val orderId = doc.id
                val uId = doc.getString("user_id") ?: ""
                val status = doc.getString("status") ?: "pending"
                val paymentMethod = doc.getString("payment_method") ?: "COD"
                val note = doc.getString("note") ?: ""
                val cancelReason = doc.getString("cancel_reason")

                val createdAt = doc.getTimestamp("created_at")
                val updatedAt = doc.getTimestamp("updated_at")

                val subtotal = (doc.get("subtotal") as? Number)?.toDouble() ?: 0.0
                val shippingFee = (doc.get("shipping_fee") as? Number)?.toDouble() ?: 30000.0
                val freeshipDiscount = (doc.get("freeship_discount") as? Number)?.toDouble() ?: 0.0
                val orderDiscount = (doc.get("order_discount") as? Number)?.toDouble() ?: 0.0
                val memberDiscount = (doc.get("member_discount") as? Number)?.toDouble() ?: 0.0
                val finalTotal = (doc.get("final_total") as? Number)?.toDouble() ?: 0.0

                val shipMap = doc.get("shipping_info") as? Map<*, *>
                val shippingInfo = ShippingInfo(
                    name = shipMap?.get("name") as? String ?: "",
                    phone = shipMap?.get("phone") as? String ?: "",
                    address = shipMap?.get("address") as? String ?: ""
                )

                val itemsRaw = doc.get("order_items") as? List<Map<*, *>>
                val orderItems = itemsRaw?.map { itemMap ->
                    OrderItem(
                        product_id = itemMap["product_id"] as? String ?: "",
                        name = itemMap["name"] as? String ?: "",
                        price = (itemMap["price"] as? Number)?.toDouble() ?: 0.0,
                        quantity = (itemMap["quantity"] as? Number)?.toInt() ?: 0,
                        image = itemMap["image"] as? String ?: "",
                        is_reviewed = itemMap["is_reviewed"] as? Boolean ?: itemMap["_reviewed"] as? Boolean ?: false
                    )
                } ?: emptyList()

                val completedOrder = Order(
                    order_id = orderId, user_id = uId, status = status, payment_method = paymentMethod,
                    subtotal = subtotal, shipping_fee = shippingFee, freeship_discount = freeshipDiscount,
                    order_discount = orderDiscount, member_discount = memberDiscount, final_total = finalTotal,
                    shipping_info = shippingInfo, note = note, order_items = orderItems,
                    cancel_reason = cancelReason, created_at = createdAt, updated_at = updatedAt
                )
                orderList.add(completedOrder)
            }
            val sortedOrders = orderList.sortedByDescending { it.created_at?.seconds ?: 0L }
            ResultState(data = sortedOrders)
        } catch (e: Exception) {
            ResultState(errorMessage = "Lỗi: ${e.message}")
        }
    }

    suspend fun getOrderById(orderId: String): ResultState<Order> {
        return try {
            val doc = db.collection("orders").document(orderId).get().await()
            if (doc.exists()) {
                val uId = doc.getString("user_id") ?: ""
                val status = doc.getString("status") ?: "pending"
                val paymentMethod = doc.getString("payment_method") ?: "COD"
                val note = doc.getString("note") ?: ""
                val cancelReason = doc.getString("cancel_reason")

                val createdAt = doc.getTimestamp("created_at")
                val updatedAt = doc.getTimestamp("updated_at")

                val subtotal = (doc.get("subtotal") as? Number)?.toDouble() ?: 0.0
                val shippingFee = (doc.get("shipping_fee") as? Number)?.toDouble() ?: 30000.0
                val freeshipDiscount = (doc.get("freeship_discount") as? Number)?.toDouble() ?: 0.0
                val orderDiscount = (doc.get("order_discount") as? Number)?.toDouble() ?: 0.0
                val memberDiscount = (doc.get("member_discount") as? Number)?.toDouble() ?: 0.0
                val finalTotal = (doc.get("final_total") as? Number)?.toDouble() ?: 0.0

                val shipMap = doc.get("shipping_info") as? Map<*, *>
                val shippingInfo = ShippingInfo(
                    name = shipMap?.get("name") as? String ?: "",
                    phone = shipMap?.get("phone") as? String ?: "",
                    address = shipMap?.get("address") as? String ?: ""
                )

                val itemsRaw = doc.get("order_items") as? List<Map<*, *>>
                val orderItems = itemsRaw?.map { itemMap ->
                    OrderItem(
                        product_id = itemMap["product_id"] as? String ?: "",
                        name = itemMap["name"] as? String ?: "",
                        price = (itemMap["price"] as? Number)?.toDouble() ?: 0.0,
                        quantity = (itemMap["quantity"] as? Number)?.toInt() ?: 0,
                        image = itemMap["image"] as? String ?: "",
                        is_reviewed = itemMap["is_reviewed"] as? Boolean ?: itemMap["_reviewed"] as? Boolean ?: false
                    )
                } ?: emptyList()

                val order = Order(
                    order_id = doc.id, user_id = uId, status = status, payment_method = paymentMethod,
                    subtotal = subtotal, shipping_fee = shippingFee, freeship_discount = freeshipDiscount,
                    order_discount = orderDiscount, member_discount = memberDiscount, final_total = finalTotal,
                    shipping_info = shippingInfo, note = note, order_items = orderItems,
                    cancel_reason = cancelReason, created_at = createdAt, updated_at = updatedAt
                )
                ResultState(data = order)
            } else {
                ResultState(errorMessage = "Không tìm thấy dữ liệu đơn hàng.")
            }
        } catch (e: Exception) {
            ResultState(errorMessage = "Lỗi máy chủ: ${e.message}")
        }
    }

    suspend fun updateOrderStatus(orderId: String, newStatus: String, reason: String = ""): ResultState<Boolean> {
        return try {
            val updates = mutableMapOf<String, Any>("status" to newStatus)
            if (reason.isNotBlank()) {
                if (newStatus == "cancelled") updates["cancel_reason"] = reason
                if (newStatus == "return_pending") updates["return_reason"] = reason
            }
            db.collection("orders").document(orderId).update(updates).await()

            // ... (Phía trên giữ nguyên)
            val orderDoc = db.collection("orders").document(orderId).get().await()
            val uId = orderDoc.getString("user_id") ?: ""
            val code = orderId.takeLast(6).uppercase()

            // 🔥 ĐÃ XÓA ICON VÀ CHUẨN HÓA VĂN BẢN
            val (title, msg) = when(newStatus) {
                "preparing" -> "Đơn hàng đang chuẩn bị" to "Người bán đang đóng gói kiện hàng #$code của bạn."
                "delivering" -> "Đơn hàng đang giao" to "Kiện hàng #$code đã được bàn giao cho shipper và đang trên đường đến bạn."
                "completed" -> "Giao nhận thành công" to "Đơn hàng #$code đã giao thành công. Hãy để lại đánh giá cho sản phẩm nhé!"
                "cancelled" -> "Đơn hàng đã hủy" to "Đơn hàng #$code đã bị hủy bỏ. Lý do: ${reason.ifBlank { "Hệ thống/Người dùng hủy" }}"
                "return_pending" -> "Yêu cầu hoàn hàng" to "Yêu cầu trả hàng/hoàn tiền đơn #$code đang chờ duyệt."
                "returned" -> "Hoàn tiền thành công" to "Hệ thống đã chấp nhận hoàn hàng và hoàn tiền cho đơn #$code."
                else -> "Cập nhật đơn hàng" to "Đơn hàng #$code của bạn vừa thay đổi trạng thái."
            }

            if (uId.isNotBlank()) {
                db.collection("notifications").document().set(
                    Notification(
                        id = db.collection("notifications").document().id, user_id = uId,
                        title = title, message = msg, type = "order", is_read = false,
                        created_at = Timestamp.now()
                    )
                ).await()
            }
            // ... (Phía dưới giữ nguyên)

            ResultState(data = true)
        } catch (e: Exception) {
            ResultState(errorMessage = "Cập nhật thất bại: ${e.message}")
        }
    }
}