package com.example.handispace.ui.user.checkout

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.handispace.data.repository.user.CartRepository
import com.example.handispace.data.repository.user.CheckoutRepository
import com.example.handispace.data.repository.user.NotificationRepository
import com.example.handispace.data.repository.user.ProductRepository
import com.example.handispace.data.repository.user.UserRepository
import com.example.handispace.data.repository.user.VoucherRepository
import com.example.handispace.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CheckoutState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false,
    val currentUser: User? = null,
    val defaultAddress: Address? = null,
    val checkoutItems: List<CartItem> = emptyList(),
    val remainingCartItems: List<CartItem> = emptyList(),
    val loyaltyDiscountPercent: Double = 0.0,
    val loyaltyLevelName: String = "", // 🔥 BỔ SUNG BIẾN NÀY ĐỂ LẤY TÊN HẠNG
    val subtotal: Double = 0.0,
    val shippingFee: Double = 30000.0,
    val memberDiscount: Double = 0.0,
    val orderDiscount: Double = 0.0,
    val freeshipDiscount: Double = 0.0,
    val finalTotal: Double = 0.0
)

@HiltViewModel
class CheckoutViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val cartRepository: CartRepository,
    private val voucherRepository: VoucherRepository,
    private val checkoutRepository: CheckoutRepository,
    private val productRepository: ProductRepository,
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    var state = mutableStateOf(CheckoutState())
        private set

    var myVouchers = mutableStateOf<List<Voucher>>(emptyList())
    var selectedFreeshipVoucher = mutableStateOf<Voucher?>(null)
    var selectedDiscountVoucher = mutableStateOf<Voucher?>(null)

    fun prepareCheckoutData(selectedItemIds: List<String>, freeshipVoucherId: String?, discountVoucherId: String?) {
        viewModelScope.launch {
            try {
                val userRes = userRepository.getUserProfile()
                val user = userRes.data ?: throw Exception("Không tải được thông tin người dùng")
                val defaultAddr = user.addresses.find { it.is_default } ?: user.addresses.firstOrNull()

                val checkoutItems: List<CartItem>
                var remainingItems: List<CartItem> = emptyList()

                if (selectedItemIds.size == 1 && selectedItemIds[0].startsWith("BUYNOW_")) {
                    val rawString = selectedItemIds[0].removePrefix("BUYNOW_")
                    val quantity = rawString.substringAfterLast("_").toIntOrNull() ?: 1
                    val productId = rawString.substringBeforeLast("_")

                    val productRes = productRepository.getProductById(productId)
                    val p = productRes.data ?: throw Exception("Không tìm thấy sản phẩm trong kho")

                    checkoutItems = listOf(CartItem(
                        product_id = p.product_id, name = p.name, price = p.price,
                        quantity = quantity, image = p.images.getOrNull(0) ?: "",
                        max_stock = p.quantity, category_id = p.category_id
                    ))
                    val cartRes = cartRepository.getCart()
                    remainingItems = cartRes.data?.items ?: emptyList()
                } else {
                    val cartRes = cartRepository.getCart()
                    val allItems = cartRes.data?.items ?: emptyList()
                    checkoutItems = allItems.filter { it.product_id in selectedItemIds }
                    remainingItems = allItems.filter { it.product_id !in selectedItemIds }
                }

                val allVouchers = voucherRepository.getActiveVouchers().data ?: emptyList()
                myVouchers.value = allVouchers.filter { it.voucher_id in user.saved_vouchers }
                selectedFreeshipVoucher.value = myVouchers.value.find { it.voucher_id == freeshipVoucherId }
                selectedDiscountVoucher.value = myVouchers.value.find { it.voucher_id == discountVoucherId }

                // 🔥 ĐÃ FIX: Tự động suy ngược tên Hạng từ phần trăm giảm giá để tránh lỗi "Unresolved reference"
                val percentInt = user.loyalty_level.discount_percent.toInt()
                val rankName = when(percentInt) {
                    8 -> "Kim Cương"
                    5 -> "Vàng"
                    2 -> "Bạc"
                    else -> "VIP"
                }

                state.value = state.value.copy(
                    isLoading = false, currentUser = user, defaultAddress = defaultAddr,
                    checkoutItems = checkoutItems, remainingCartItems = remainingItems,
                    loyaltyDiscountPercent = user.loyalty_level.discount_percent,
                    loyaltyLevelName = rankName // 🔥 Truyền tên vừa tự dịch vào đây
                )
                recalculateTotals()
            } catch (e: Exception) {
                state.value = state.value.copy(isLoading = false, errorMessage = e.message)
            }
        }
    }

    fun clearMessage() {
        state.value = state.value.copy(errorMessage = null, isSuccess = false)
    }

    fun isVoucherApplicable(voucher: Voucher): Boolean {
        if (voucher.usage_limit > 0 && voucher.used_count >= voucher.usage_limit) return false
        if (voucher.applicable_categories.isEmpty()) return true
        return state.value.checkoutItems.any { it.category_id in voucher.applicable_categories }
    }

    private fun recalculateTotals() {
        val currentState = state.value
        val subtotal = currentState.checkoutItems.sumOf { it.price * it.quantity }

        val memberDiscount = subtotal * (currentState.loyaltyDiscountPercent / 100.0)
        val subtotalAfterLoyalty = subtotal - memberDiscount

        var orderDiscount = 0.0
        var freeshipDiscount = 0.0
        val shippingFee = 30000.0

        selectedFreeshipVoucher.value?.let { v ->
            if (subtotal >= v.min_order_value && isVoucherApplicable(v)) {
                freeshipDiscount = if (v.discount_type == "percent") (shippingFee * v.discount_value / 100.0) else v.discount_value
                freeshipDiscount = minOf(freeshipDiscount, shippingFee)
            } else { selectedFreeshipVoucher.value = null }
        }

        selectedDiscountVoucher.value?.let { v ->
            if (subtotalAfterLoyalty >= v.min_order_value && isVoucherApplicable(v)) {
                val calc = if (v.discount_type == "percent") {
                    val pVal = subtotalAfterLoyalty * (v.discount_value / 100.0)
                    if (v.max_discount > 0) minOf(pVal, v.max_discount) else pVal
                } else v.discount_value
                orderDiscount = minOf(calc, subtotalAfterLoyalty)
            } else { selectedDiscountVoucher.value = null }
        }

        val finalTotal = (subtotalAfterLoyalty - orderDiscount).coerceAtLeast(0.0) + (shippingFee - freeshipDiscount).coerceAtLeast(0.0)

        state.value = currentState.copy(subtotal = subtotal, shippingFee = shippingFee, memberDiscount = memberDiscount, orderDiscount = orderDiscount, freeshipDiscount = freeshipDiscount, finalTotal = finalTotal)
    }

    fun selectAddress(addressId: String) {
        val user = state.value.currentUser ?: return
        user.addresses.find { it.address_id == addressId }?.let { state.value = state.value.copy(defaultAddress = it) }
    }

    fun selectFreeshipVoucher(v: Voucher?) { selectedFreeshipVoucher.value = v; recalculateTotals() }
    fun selectDiscountVoucher(v: Voucher?) { selectedDiscountVoucher.value = v; recalculateTotals() }

    fun autoSelectBestVouchers() {
        val avail = myVouchers.value
        val currentState = state.value
        val subtotal = currentState.checkoutItems.sumOf { it.price * it.quantity }
        val subtotalAfterLoyalty = subtotal - (subtotal * (currentState.loyaltyDiscountPercent / 100.0))

        selectedFreeshipVoucher.value = avail.filter { it.type == "shipping" && subtotal >= it.min_order_value && isVoucherApplicable(it) }.maxByOrNull { it.discount_value }

        selectedDiscountVoucher.value = avail.filter { it.type == "order" && subtotalAfterLoyalty >= it.min_order_value && isVoucherApplicable(it) }.maxByOrNull {
            if (it.discount_type == "percent") (subtotalAfterLoyalty * it.discount_value / 100.0) else it.discount_value
        }
        recalculateTotals()
    }

    fun applyVoucherByCode(code: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val result = voucherRepository.saveVoucherByCode(code)
            val successMessage = result.data

            if (successMessage != null) {
                val user = userRepository.getUserProfile().data
                val all = voucherRepository.getActiveVouchers().data ?: emptyList()
                if (user != null) myVouchers.value = all.filter { it.voucher_id in user.saved_vouchers }
                autoSelectBestVouchers()
                onResult(true, successMessage)
            } else {
                onResult(false, result.errorMessage ?: "Mã không hợp lệ")
            }
        }
    }

    fun placeOrder(note: String, paymentMethod: String) {
        val curr = state.value
        val addr = curr.defaultAddress ?: return run { state.value = curr.copy(errorMessage = "Vui lòng chọn địa chỉ!") }

        viewModelScope.launch {
            state.value = curr.copy(isLoading = true)
            val orderItems = curr.checkoutItems.map { OrderItem(it.product_id, it.name, it.price, it.quantity, it.image) }
            val shipInfo = ShippingInfo(addr.receiver_name, addr.receiver_phone, "${addr.street_address}, ${addr.ward}, ${addr.province}")

            val newOrder = Order(payment_method = paymentMethod, shipping_info = shipInfo, note = note, order_items = orderItems, subtotal = curr.subtotal, shipping_fee = curr.shippingFee, freeship_discount = curr.freeshipDiscount, order_discount = curr.orderDiscount, member_discount = curr.memberDiscount, final_total = curr.finalTotal, status = "pending")

            val appliedVouchers = mutableListOf<Voucher>().apply {
                selectedFreeshipVoucher.value?.let { add(it) }
                selectedDiscountVoucher.value?.let { add(it) }
            }

            val result = checkoutRepository.placeOrder(newOrder, curr.remainingCartItems, appliedVouchers, curr.currentUser!!)

            if (result.data != null) {
                state.value = curr.copy(isLoading = false, isSuccess = true)

                val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: ""
                if (uid.isNotBlank()) {
                    val shortCode = newOrder.order_id.takeLast(6).uppercase()
                    notificationRepository.pushNotification(
                        userId = uid,
                        title = "Đặt hàng thành công",
                        message = "Đơn hàng đã đặt thành công, đang chờ xác nhận."
                    )
                }
            } else {
                state.value = curr.copy(isLoading = false, errorMessage = result.errorMessage)
            }
        }
    }
}