package com.example.handispace.ui.user.review

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.handispace.data.repository.user.OrderRepository
import com.example.handispace.data.repository.user.ReviewRepository
import com.example.handispace.model.Order
import com.example.handispace.model.Review // <-- Nãy ní thiếu dòng này nè
import com.example.handispace.utils.ResultState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReviewOrderViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
    private val reviewRepository: ReviewRepository
) : ViewModel() {

    var orderState = mutableStateOf(ResultState<Order>())
        private set
    var submitState = mutableStateOf(ResultState<Boolean>())
        private set

    var reviewsState = mutableStateOf(ResultState<List<Review>>())
        private set

    fun getOrderDetail(orderId: String) {
        viewModelScope.launch {
            orderState.value = ResultState(isLoading = true)
            val orderRes = orderRepository.getOrderById(orderId)
            orderState.value = orderRes

            if (orderRes.data != null) {
                reviewsState.value = reviewRepository.getOrderReviews(orderId)
            }
        }
    }

    fun submitReview(orderId: String, productId: String, rating: Double, content: String) {
        viewModelScope.launch {
            submitState.value = ResultState(isLoading = true)
            val res = reviewRepository.submitReview(orderId, productId, rating, content)
            submitState.value = res
            if (res.data == true) {
                getOrderDetail(orderId)
            }
        }
    }
    fun resetSubmitState() { submitState.value = ResultState() }
}