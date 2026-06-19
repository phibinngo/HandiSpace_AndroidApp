package com.example.handispace.ui.user.product_detail

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.handispace.data.repository.user.CartRepository
import com.example.handispace.data.repository.user.ProductRepository
import com.example.handispace.model.CartItem
import com.example.handispace.model.Product
import com.example.handispace.utils.ResultState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.handispace.data.repository.user.ReviewRepository
import com.example.handispace.model.Review

@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val cartRepository: CartRepository,
    private val reviewRepository: ReviewRepository //
) : ViewModel() {

    var productState = mutableStateOf(ResultState<Product>())
        private set

    var addToCartState = mutableStateOf(ResultState<String>())
        private set

    var reviewsState = mutableStateOf(ResultState<List<Review>>())
        private set

    // 🔥 BỔ SUNG: State để hứng danh sách sản phẩm gợi ý (cùng danh mục)
    var suggestedProductsState = mutableStateOf(ResultState<List<Product>>())
        private set

    fun getProductDetail(productId: String) {
        viewModelScope.launch {
            productState.value = ResultState(isLoading = true)
            reviewsState.value = ResultState(isLoading = true)
            suggestedProductsState.value = ResultState(isLoading = true) // Cờ loading gợi ý

            val prodResult = productRepository.getProductById(productId)
            productState.value = prodResult

            // Gọi lấy danh sách review
            reviewsState.value = reviewRepository.getProductReviews(productId)

            // 🔥 ĐÃ FIX LỖI SMART CAST: Hứng data vào biến val cố định trước khi kiểm tra
            val currentProduct = prodResult.data

            if (currentProduct != null) {
                val categoryId = currentProduct.category_id

                // Mượn hàm lấy tất cả sản phẩm
                val allProductsResult = productRepository.getProducts("all")

                // 🔥 LẠI TIẾP TỤC HỨNG DATA VÀO BIẾN VAL ĐỂ TRÁNH LỖI SMART CAST
                val allProducts = allProductsResult.data

                if (allProducts != null) {
                    val filteredSuggestions = allProducts
                        .filter { it.category_id == categoryId && it.product_id != productId }
                        .shuffled() // Đảo lộn ngẫu nhiên cho phong phú
                        .take(10) // Tạm thời lấy tối đa 10 cái cho đỡ nặng

                    suggestedProductsState.value = ResultState(data = filteredSuggestions)
                } else {
                    suggestedProductsState.value = ResultState(errorMessage = "Không tải được gợi ý")
                }
            } else {
                suggestedProductsState.value = ResultState(errorMessage = "Lỗi tải sản phẩm gốc")
            }
        }
    }

    fun addToCart(product: Product, quantity: Int) {
        viewModelScope.launch {
            addToCartState.value = ResultState(isLoading = true)

            // 1. CHỌC GIỎ HÀNG XEM KHÁCH CÓ BAO NHIÊU RỒI
            val cartResult = cartRepository.getCart()
            val existingCartItem = cartResult.data?.items?.find { it.product_id == product.product_id }
            val currentInCart = existingCartItem?.quantity ?: 0

            // 2. LOGIC BÁO LỖI SIÊU CHUYÊN NGHIỆP CỦA NÍ
            if (currentInCart + quantity > product.quantity) {
                val errorMsg = if (currentInCart > 0) {
                    "Bạn đã có $currentInCart sản phẩm trong giỏ. Không thể thêm vượt quá số lượng kho (${product.quantity})!"
                } else {
                    "Rất tiếc! Số lượng hàng còn lại chỉ là ${product.quantity}."
                }

                addToCartState.value = ResultState(errorMessage = errorMsg)
                return@launch
            }

            // Ép dữ liệu thành 1 món hàng
            val imageUrl = if (product.images.isNotEmpty()) product.images[0] else ""
            val cartItem = CartItem(
                product_id = product.product_id,
                name = product.name,
                price = product.price,
                quantity = quantity,
                image = imageUrl,
                max_stock = product.quantity
            )

            addToCartState.value = cartRepository.addToCart(cartItem)
        }
    }

    fun resetAddToCartState() {
        addToCartState.value = ResultState()
    }
}