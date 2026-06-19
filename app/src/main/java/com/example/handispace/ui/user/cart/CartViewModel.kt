package com.example.handispace.ui.user.cart

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.handispace.data.repository.user.CartRepository
import com.example.handispace.data.repository.user.ProductRepository
import com.example.handispace.data.repository.user.UserRepository
import com.example.handispace.data.repository.user.VoucherRepository
import com.example.handispace.model.Cart
import com.example.handispace.model.Voucher
import com.example.handispace.utils.ResultState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CartViewModel @Inject constructor(
    private val cartRepository: CartRepository,
    private val productRepository: ProductRepository,
    private val userRepository: UserRepository,
    private val voucherRepository: VoucherRepository
) : ViewModel() {

    var cartState = mutableStateOf(ResultState<Cart>())
        private set

    var myVouchers = mutableStateOf<List<Voucher>>(emptyList())
    var selectedFreeshipVoucher = mutableStateOf<Voucher?>(null)
    var selectedDiscountVoucher = mutableStateOf<Voucher?>(null)

    init {
        loadCart()
    }

    fun loadCart() {
        viewModelScope.launch {
            cartState.value = ResultState(isLoading = true)
            val result = cartRepository.getCart()
            val cart = result.data

            if (cart != null) {
                val updatedItems = cart.items.map { item ->
                    val productRes = productRepository.getProductById(item.product_id)
                    val realStock = productRes.data?.quantity ?: 0
                    val realCategoryId = productRes.data?.category_id ?: ""
                    val safeQuantity = if (item.quantity > realStock) realStock else item.quantity

                    item.copy(
                        quantity = safeQuantity,
                        max_stock = realStock,
                        category_id = if (item.category_id.isEmpty()) realCategoryId else item.category_id
                    )
                }.sortedBy { it.max_stock == 0 }

                val isChanged = updatedItems.any {
                    val old = cart.items.find { o -> o.product_id == it.product_id }
                    it.quantity != old?.quantity || it.category_id != old?.category_id
                }

                if (isChanged) {
                    cartRepository.updateCartItems(updatedItems)
                }

                cartState.value = ResultState(data = cart.copy(items = updatedItems))
                loadUserVouchers()
            } else {
                cartState.value = ResultState(errorMessage = result.errorMessage)
            }
        }
    }

    private suspend fun loadUserVouchers() {
        val userResult = userRepository.getUserProfile()
        val userProfile = userResult.data
        if (userProfile != null && userProfile.saved_vouchers.isNotEmpty()) {
            val allVouchersRes = voucherRepository.getActiveVouchers()
            val allVouchers = allVouchersRes.data
            if (allVouchers != null) {
                myVouchers.value = allVouchers.filter { it.voucher_id in userProfile.saved_vouchers }
            }
        }
    }

    fun isVoucherApplicable(voucher: Voucher, selectedProductIds: Set<String>): Boolean {
        if (voucher.usage_limit > 0 && voucher.used_count >= voucher.usage_limit) return false
        if (voucher.applicable_categories.isEmpty()) return true

        val currentCart = cartState.value.data ?: return false
        val selectedItems = currentCart.items.filter { it.product_id in selectedProductIds }

        return selectedItems.any { item -> voucher.applicable_categories.contains(item.category_id) }
    }

    fun autoSelectBestVouchers(totalPrice: Double, selectedProductIds: Set<String>) {
        if (totalPrice <= 0) return
        val available = myVouchers.value

        selectedFreeshipVoucher.value = available
            .filter { it.type == "shipping" && totalPrice >= it.min_order_value && isVoucherApplicable(it, selectedProductIds) }
            .maxByOrNull { it.discount_value }

        selectedDiscountVoucher.value = available
            .filter { it.type == "order" && totalPrice >= it.min_order_value && isVoucherApplicable(it, selectedProductIds) }
            .maxByOrNull { calculateDiscountValue(it, totalPrice) }
    }

    fun calculateDiscountValue(voucher: Voucher, totalPrice: Double): Double {
        if (totalPrice < voucher.min_order_value) return 0.0
        return if (voucher.discount_type == "percent") {
            val pctValue = totalPrice * (voucher.discount_value / 100.0)
            if (voucher.max_discount > 0 && pctValue > voucher.max_discount) voucher.max_discount else pctValue
        } else {
            voucher.discount_value
        }
    }

    fun applyVoucherByCode(code: String, totalPrice: Double, selectedProductIds: Set<String>, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val result = voucherRepository.saveVoucherByCode(code)
            val successMsg = result.data
            if (successMsg != null) {
                loadUserVouchers()
                val appliedVoucher = myVouchers.value.find { it.code.equals(code, ignoreCase = true) }
                if (appliedVoucher != null && !isVoucherApplicable(appliedVoucher, selectedProductIds)) {
                    onResult(false, "Voucher này không áp dụng cho sản phẩm đang chọn hoặc đã hết lượt!")
                    return@launch
                }
                autoSelectBestVouchers(totalPrice, selectedProductIds)
                onResult(true, successMsg)
            } else {
                onResult(false, result.errorMessage ?: "Mã giảm giá không hợp lệ")
            }
        }
    }

    fun changeQuantity(productId: String, isIncrease: Boolean) {
        val currentCart = cartState.value.data ?: return
        val currentItems = currentCart.items.toMutableList()
        val itemIndex = currentItems.indexOfFirst { it.product_id == productId }
        if (itemIndex != -1) {
            val item = currentItems[itemIndex]
            val newQuantity = if (isIncrease) item.quantity + 1 else item.quantity - 1
            if (newQuantity > 0) {
                currentItems[itemIndex] = item.copy(quantity = newQuantity)
                updateCartToFirebase(currentCart.copy(items = currentItems))
            }
        }
    }

    fun removeItem(productId: String) {
        val currentCart = cartState.value.data ?: return
        val updatedItems = currentCart.items.filter { it.product_id != productId }
        updateCartToFirebase(currentCart.copy(items = updatedItems))
    }

    private fun updateCartToFirebase(newCart: Cart) {
        viewModelScope.launch {
            cartState.value = ResultState(data = newCart)
            cartRepository.updateCartItems(newCart.items)
            selectedFreeshipVoucher.value = null
            selectedDiscountVoucher.value = null
        }
    }
}