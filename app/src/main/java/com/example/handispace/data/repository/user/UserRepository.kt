package com.example.handispace.data.repository.user

import com.example.handispace.model.Address
import com.example.handispace.model.LoyaltyLevel
import com.example.handispace.model.User
import com.example.handispace.utils.Constants
import com.example.handispace.utils.ResultState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) {
    private fun calculateLoyaltyLevel(totalSpent: Double): LoyaltyLevel {
        return when {
            totalSpent >= 10_000_000 -> LoyaltyLevel("Kim Cương", 8.0)
            totalSpent >= 5_000_000 -> LoyaltyLevel("Vàng", 5.0)
            totalSpent >= 2_000_000 -> LoyaltyLevel("Bạc", 2.0)
            else -> LoyaltyLevel("Thành viên", 0.0)
        }
    }

    suspend fun getUserProfile(): ResultState<User> {
        return try {
            val userId = auth.currentUser?.uid ?: return ResultState(errorMessage = "Người dùng chưa đăng nhập.")

            val snapshot = db.collection(Constants.USERS).document(userId).get().await()
            var user = snapshot.toObject(User::class.java)

            if (user != null) {
                val correctRank = calculateLoyaltyLevel(user.total_spent)

                if (user.loyalty_level.level_name != correctRank.level_name || user.loyalty_level.discount_percent != correctRank.discount_percent) {
                    user = user.copy(loyalty_level = correctRank)
                    db.collection(Constants.USERS).document(userId).update("loyalty_level", correctRank)
                }

                ResultState(data = user)
            } else {
                ResultState(errorMessage = "Không tìm thấy thông tin người dùng.")
            }
        } catch (e: Exception) {
            ResultState(errorMessage = e.message ?: "Đã xảy ra lỗi khi lấy thông tin.")
        }
    }

    suspend fun updateUser(user: User): ResultState<Boolean> {
        return try {
            val userId = auth.currentUser?.uid ?: return ResultState(errorMessage = "Chưa đăng nhập")
            db.collection(Constants.USERS).document(userId).set(user).await()
            ResultState(data = true)
        } catch (e: Exception) {
            ResultState(errorMessage = e.message ?: "Lỗi không xác định khi cập nhật.")
        }
    }

    suspend fun updateUserAddresses(addresses: List<Address>): ResultState<Boolean> {
        return try {
            val userId = auth.currentUser?.uid ?: return ResultState(errorMessage = "Chưa đăng nhập")
            db.collection(Constants.USERS).document(userId).update("addresses", addresses).await()
            ResultState(data = true)
        } catch (e: Exception) {
            ResultState(errorMessage = e.message ?: "Lỗi cập nhật địa chỉ.")
        }
    }

    fun logout() {
        auth.signOut()
    }
}