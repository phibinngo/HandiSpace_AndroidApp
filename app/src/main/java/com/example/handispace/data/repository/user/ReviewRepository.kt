package com.example.handispace.data.repository.user

import com.example.handispace.model.Review
import com.example.handispace.utils.ResultState
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ReviewRepository @Inject constructor(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    suspend fun submitReview(orderId: String, productId: String, rating: Double, content: String): ResultState<Boolean> {
        return try {
            val userId = auth.currentUser?.uid ?: return ResultState(errorMessage = "Chưa đăng nhập")
            val userDoc = db.collection("users").document(userId).get().await()
            val userName = userDoc.getString("username") ?: "Khách hàng"

            val reviewRef = db.collection("reviews").document()
            val newReview = Review(
                review_id = reviewRef.id, product_id = productId, user_id = userId,
                user_name = userName, order_id = orderId, rating = rating,
                content = content, created_at = Timestamp.now()
            )

            val orderRef = db.collection("orders").document(orderId)

            db.runTransaction { transaction ->
                val snapshot = transaction.get(orderRef)

                val itemsRaw = snapshot.get("order_items") as? List<Map<String, Any>> ?: emptyList()
                val updatedItems = itemsRaw.map { item ->
                    if (item["product_id"] == productId) {
                        item.toMutableMap().apply {
                            put("is_reviewed", true)
                            put("_reviewed", true)
                        }
                    } else item
                }

                transaction.set(reviewRef, newReview)
                transaction.update(orderRef, "order_items", updatedItems)
            }.await()

            // ==========================================
            // 🔥 2. TỰ ĐỘNG TÍNH LẠI RATING VÀ LƯU VÀO PRODUCT
            // ==========================================
            val reviewsSnap = db.collection("reviews").whereEqualTo("product_id", productId).get().await()

            var totalStars = 0.0
            val count = reviewsSnap.size()

            for (doc in reviewsSnap.documents) {
                val r = doc.getDouble("rating") ?: 0.0
                totalStars += r
            }

            val newAvg = if (count > 0) totalStars / count else 0.0

            // Cập nhật ngược lại vào bảng products
            db.collection("products").document(productId).update(
                "rating_average", newAvg,
                "review_count", count
            ).await()
            // ==========================================

            ResultState(data = true)
        } catch (e: Exception) {
            ResultState(errorMessage = "Lỗi gửi đánh giá: ${e.message}")
        }
    }

    suspend fun getProductReviews(productId: String): ResultState<List<Review>> {
        return try {
            val snapshot = db.collection("reviews").whereEqualTo("product_id", productId).get().await()
            val reviews = snapshot.toObjects(Review::class.java).sortedByDescending { it.created_at?.seconds ?: 0L }
            ResultState(data = reviews)
        } catch (e: Exception) {
            ResultState(errorMessage = "Lỗi tải đánh giá: ${e.message}")
        }
    }

    suspend fun getOrderReviews(orderId: String): ResultState<List<Review>> {
        return try {
            val userId = auth.currentUser?.uid ?: return ResultState(errorMessage = "Chưa đăng nhập")
            val snapshot = db.collection("reviews")
                .whereEqualTo("order_id", orderId)
                .whereEqualTo("user_id", userId)
                .get()
                .await()
            val reviews = snapshot.toObjects(Review::class.java)
            ResultState(data = reviews)
        } catch (e: Exception) {
            ResultState(errorMessage = "Lỗi tải lịch sử đánh giá: ${e.message}")
        }
    }
}