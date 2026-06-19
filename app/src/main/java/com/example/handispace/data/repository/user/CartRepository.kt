package com.example.handispace.data.repository.user

import com.example.handispace.model.Cart
import com.example.handispace.model.CartItem
import com.example.handispace.utils.Constants
import com.example.handispace.utils.ResultState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class CartRepository @Inject constructor(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    suspend fun getCart(): ResultState<Cart> {
        val userId = auth.currentUser?.uid ?: return ResultState(errorMessage = "Vui lòng đăng nhập!")

        return try {
            val snapshot = db.collection(Constants.CARTS).document(userId).get().await()
            if (snapshot.exists()) {
                val cart = snapshot.toObject(Cart::class.java)
                ResultState(data = cart)
            } else {
                ResultState(data = Cart(user_id = userId))
            }
        } catch (e: Exception) {
            ResultState(errorMessage = e.message ?: "Lỗi tải giỏ hàng.")
        }
    }

    suspend fun addToCart(cartItem: CartItem): ResultState<String> {
        val userId = auth.currentUser?.uid ?: return ResultState(errorMessage = "Vui lòng đăng nhập!")
        val cartRef = db.collection(Constants.CARTS).document(userId)

        return try {
            val snapshot = cartRef.get().await()

            if (snapshot.exists()) {
                val currentCart = snapshot.toObject(Cart::class.java)
                val existingItem = currentCart?.items?.find { it.product_id == cartItem.product_id }

                if (existingItem != null) {
                    val updatedItems = currentCart.items.map {
                        if (it.product_id == cartItem.product_id) {
                            it.copy(quantity = it.quantity + cartItem.quantity)
                        } else it
                    }
                    cartRef.update("items", updatedItems, "updated_at", FieldValue.serverTimestamp()).await()
                } else {
                    cartRef.update("items", FieldValue.arrayUnion(cartItem), "updated_at", FieldValue.serverTimestamp()).await()
                }
            } else {
                val newCart = Cart(user_id = userId, items = listOf(cartItem))
                cartRef.set(newCart).await()
            }
            ResultState(data = "Đã thêm vào giỏ hàng thành công!")
        } catch (e: Exception) {
            ResultState(errorMessage = "Lỗi thêm giỏ hàng: ${e.message}")
        }
    }

    suspend fun updateCartItems(newItems: List<CartItem>): ResultState<String> {
        val userId = auth.currentUser?.uid ?: return ResultState(errorMessage = "Lỗi xác thực!")
        return try {
            db.collection(Constants.CARTS).document(userId).update(
                "items", newItems,
                "updated_at", FieldValue.serverTimestamp()
            ).await()
            ResultState(data = "Cập nhật giỏ hàng thành công")
        } catch (e: Exception) {
            ResultState(errorMessage = e.message ?: "Lỗi cập nhật giỏ hàng")
        }
    }
}