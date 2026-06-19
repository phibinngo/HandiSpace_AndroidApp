package com.example.handispace.ui.user.order.detail

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.handispace.data.repository.user.CartRepository
import com.example.handispace.data.repository.user.OrderRepository
import com.example.handispace.data.repository.user.ProductRepository
import com.example.handispace.model.CartItem
import com.example.handispace.model.Order
import com.example.handispace.utils.ResultState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OrderDetailViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
    private val cartRepository: CartRepository,
    private val productRepository: ProductRepository
) : ViewModel() {

    var orderState = mutableStateOf(ResultState<Order>())
        private set

    var successMessage = mutableStateOf<String?>(null)
    var errorMessage = mutableStateOf<String?>(null)

    fun getOrderDetail(orderId: String) {
        viewModelScope.launch {
            orderState.value = ResultState(isLoading = true)
            orderState.value = orderRepository.getOrderById(orderId)
        }
    }

    fun updateOrderStatusWithReason(orderId: String, newStatus: String, reason: String = "") {
        viewModelScope.launch {
            val result = orderRepository.updateOrderStatus(orderId, newStatus, reason)
            if (result.data == true) {
                successMessage.value = when(newStatus) {
                    "cancelled" -> "Hủy đơn hàng thành công!"
                    "return_pending" -> "Đã gửi yêu cầu trả hàng, vui lòng đợi Admin duyệt!"
                    "completed" -> "Xác nhận đã nhận hàng thành công!"
                    else -> "Cập nhật trạng thái thành công!"
                }
                getOrderDetail(orderId)
            } else {
                errorMessage.value = result.errorMessage
            }
        }
    }

    fun buyAgain(order: Order) {
        viewModelScope.launch {
            try {
                val cartRes = cartRepository.getCart()
                val currentCartItems = cartRes.data?.items?.toMutableList() ?: mutableListOf()
                val outOfStockItems = mutableListOf<String>()

                order.order_items.forEach { orderedItem ->
                    val pRes = productRepository.getProductById(orderedItem.product_id).data
                    if (pRes != null && pRes.quantity > 0) {
                        val existingIndex = currentCartItems.indexOfFirst { it.product_id == pRes.product_id }
                        val addQuantity = minOf(orderedItem.quantity, pRes.quantity)

                        if (existingIndex >= 0) {
                            val oldItem = currentCartItems[existingIndex]
                            val newQuantity = minOf(oldItem.quantity + addQuantity, pRes.quantity)
                            currentCartItems[existingIndex] = oldItem.copy(quantity = newQuantity, max_stock = pRes.quantity)
                        } else {
                            currentCartItems.add(
                                CartItem(
                                    product_id = pRes.product_id, name = pRes.name, price = pRes.price,
                                    quantity = addQuantity, image = pRes.images.firstOrNull() ?: "",
                                    max_stock = pRes.quantity, category_id = pRes.category_id
                                )
                            )
                        }
                    } else { outOfStockItems.add(orderedItem.name) }
                }

                cartRepository.updateCartItems(currentCartItems)

                // ĐÃ SỬA TEXT Ở ĐÂY NHƯ Ý NÍ:
                if (outOfStockItems.isEmpty()) successMessage.value = "Đã thêm lại các sản phẩm của đơn hàng vào giỏ!"
                else errorMessage.value = "Đã thêm các món còn hàng. Bị thiếu: ${outOfStockItems.joinToString(", ")}"
            } catch (e: Exception) {
                errorMessage.value = "Lỗi luồng mua lại: ${e.message}"
            }
        }
    }
    fun clearMessages() { successMessage.value = null; errorMessage.value = null }
}