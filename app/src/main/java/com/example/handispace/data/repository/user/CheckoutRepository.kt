package com.example.handispace.data.repository.user

import com.example.handispace.model.*
import com.example.handispace.utils.ResultState
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class CheckoutRepository @Inject constructor(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private fun calculateLoyaltyLevel(totalSpent: Double): LoyaltyLevel {
        return when {
            totalSpent >= 10_000_000 -> LoyaltyLevel("Kim Cương", 8.0)
            totalSpent >= 5_000_000 -> LoyaltyLevel("Vàng", 5.0)
            totalSpent >= 2_000_000 -> LoyaltyLevel("Bạc", 2.0)
            else -> LoyaltyLevel("Thành viên", 0.0)
        }
    }

    suspend fun placeOrder(
        newOrder: Order,
        remainingCartItems: List<CartItem>,
        appliedVouchers: List<Voucher>,
        currentUser: User
    ): ResultState<String> {
        return try {
            val userId = auth.currentUser?.uid ?: return ResultState(errorMessage = "Vui lòng đăng nhập!")
            val batch = db.batch()
            val userRef = db.collection("users").document(userId)

            val orderRef = db.collection("orders").document()
            val finalOrder = newOrder.copy(
                order_id = orderRef.id,
                user_id = userId,
                status = "pending",
                created_at = Timestamp.now(),
                updated_at = Timestamp.now()
            )
            batch.set(orderRef, finalOrder)

            // 🔥 FIX BUG CẬP NHẬT KHO VÀ LƯỢT BÁN
            finalOrder.order_items.forEach { item ->
                val productRef = db.collection("products").document(item.product_id)
                // Trừ số lượng kho
                batch.update(productRef, "quantity", FieldValue.increment(-item.quantity.toLong()))
                // Cộng số lượt đã bán
                batch.update(productRef, "sold_count", FieldValue.increment(item.quantity.toLong()))
            }

            batch.update(db.collection("carts").document(userId), "items", remainingCartItems)

            appliedVouchers.forEach { voucher ->
                val history = UsedVoucherInfo(voucher_id = voucher.voucher_id, used_at = Timestamp.now())
                batch.update(userRef, "used_vouchers", FieldValue.arrayUnion(history))

                if (voucher.type != "shipping") {
                    batch.update(userRef, "saved_vouchers", FieldValue.arrayRemove(voucher.voucher_id))
                }
                batch.update(db.collection("vouchers").document(voucher.voucher_id), "used_count", FieldValue.increment(1))
            }

            batch.commit().await()
            ResultState(data = orderRef.id)
        } catch (e: Exception) {
            ResultState(errorMessage = "Lỗi chốt đơn: ${e.message}")
        }
    }

    suspend fun completeOrderAndUpdateLoyalty(orderId: String, userId: String, eligibleSpentAmount: Double): ResultState<Boolean> {
        return try {
            val batch = db.batch()
            val orderRef = db.collection("orders").document(orderId)
            val userRef = db.collection("users").document(userId)

            batch.update(orderRef, "status", "completed", "updated_at", Timestamp.now())

            val userSnapshot = userRef.get().await()
            val currentTotalSpent = userSnapshot.getDouble("total_spent") ?: 0.0

            val newTotalSpent = currentTotalSpent + eligibleSpentAmount
            val newRank = calculateLoyaltyLevel(newTotalSpent)

            batch.update(userRef, "total_spent", newTotalSpent, "loyalty_level", newRank)

            batch.commit().await()
            ResultState(data = true)
        } catch (e: Exception) {
            ResultState(errorMessage = "Lỗi cập nhật hoàn thành đơn hàng: ${e.message}")
        }
    }

    suspend fun cancelOrRefundOrder(orderId: String, userId: String, isRefund: Boolean = false): ResultState<Boolean> {
        return try {
            val batch = db.batch()
            val orderRef = db.collection("orders").document(orderId)
            val userRef = db.collection("users").document(userId)

            val orderSnapshot = orderRef.get().await()
            val currentStatus = orderSnapshot.getString("status")
            val subtotal = orderSnapshot.getDouble("subtotal") ?: 0.0
            val orderDiscount = orderSnapshot.getDouble("order_discount") ?: 0.0
            val memberDiscount = orderSnapshot.getDouble("member_discount") ?: 0.0

            val eligibleSpentAmount = (subtotal - orderDiscount - memberDiscount).coerceAtLeast(0.0)

            val orderItems = orderSnapshot.get("order_items") as? List<Map<String, Any>> ?: emptyList()

            // 🔥 FIX BUG HOÀN KHO VÀ TRỪ LƯỢT BÁN KHI HỦY
            for (item in orderItems) {
                val productId = item["product_id"] as? String ?: continue
                val quantity = (item["quantity"] as? Long) ?: 0L
                val productRef = db.collection("products").document(productId)

                // Trả lại số lượng kho
                batch.update(productRef, "quantity", FieldValue.increment(quantity))
                // Trừ lại lượt bán (vì khách không mua nữa)
                batch.update(productRef, "sold_count", FieldValue.increment(-quantity))
            }

            if (currentStatus == "completed") {
                val userSnapshot = userRef.get().await()
                val currentTotalSpent = userSnapshot.getDouble("total_spent") ?: 0.0

                val newTotalSpent = (currentTotalSpent - eligibleSpentAmount).coerceAtLeast(0.0)
                val newRank = calculateLoyaltyLevel(newTotalSpent)

                batch.update(userRef, "total_spent", newTotalSpent, "loyalty_level", newRank)
            }

            val newStatus = if (isRefund) "refunded" else "cancelled"
            batch.update(orderRef, "status", newStatus, "updated_at", Timestamp.now())

            batch.commit().await()
            ResultState(data = true)
        } catch (e: Exception) {
            ResultState(errorMessage = "Lỗi xử lý hủy/hoàn đơn: ${e.message}")
        }
    }
}