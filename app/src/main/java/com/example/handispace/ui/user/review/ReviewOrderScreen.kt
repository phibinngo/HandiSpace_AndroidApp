package com.example.handispace.ui.user.review

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.handispace.model.OrderItem
import com.example.handispace.model.Review
import com.example.handispace.ui.components.ErrorPopup
import com.example.handispace.ui.components.SuccessPopup
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewOrderScreen(navController: NavController, orderId: String, viewModel: ReviewOrderViewModel) {
    LaunchedEffect(orderId) { viewModel.getOrderDetail(orderId) }

    val state = viewModel.orderState.value
    val order = state.data
    val submitState = viewModel.submitState.value

    // Lấy list review đã viết từ state của ViewModel về
    val writtenReviews = viewModel.reviewsState.value.data ?: emptyList()

    val orange600 = Color(0xFFEA580C)

    var showSuccess by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    LaunchedEffect(submitState.data, submitState.errorMessage) {
        if (submitState.data == true) {
            showSuccess = true
            viewModel.resetSubmitState()
            delay(1500)
            showSuccess = false
        } else if (submitState.errorMessage != null && submitState.errorMessage!!.isNotEmpty()) {
            errorMessage = submitState.errorMessage!!
            showError = true
            viewModel.resetSubmitState()
            delay(1500)
            showError = false
        }
    }

    if (showSuccess) SuccessPopup("Cảm ơn bạn đã đánh giá!", onDismiss = { showSuccess = false })
    if (showError) ErrorPopup(errorMessage, onDismiss = { showError = false })

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Đánh giá sản phẩm", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Filled.ArrowBack, contentDescription = null, tint = orange600) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF9FAFB)
    ) { padding ->
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = orange600) }
        } else if (order != null) {
            val unreviewed = order.order_items.filter { !it.is_reviewed }
            val reviewed = order.order_items.filter { it.is_reviewed }

            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
                // 1. KHỐI CHƯA ĐÁNH GIÁ (NẰM TRÊN)
                items(unreviewed, key = { it.product_id }) { item ->
                    ReviewItemCard(
                        item = item, isReviewed = false, isLoading = submitState.isLoading, myReview = null
                    ) { rating, cmt ->
                        viewModel.submitReview(order.order_id, item.product_id, rating, cmt)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // 2. KHỐI ĐÃ ĐÁNH GIÁ (CHÌM XUỐNG DƯỚI)
                if (reviewed.isNotEmpty()) {
                    item { Text("Sản phẩm đã đánh giá", color = Color.Gray, modifier = Modifier.padding(bottom = 8.dp), fontSize = 14.sp) }
                    items(reviewed, key = { "reviewed_${it.product_id}" }) { item ->
                        // Tìm đúng cái Review của món hàng này để truyền vào giao diện khóa
                        val matchedReview = writtenReviews.find { it.product_id == item.product_id }

                        ReviewItemCard(
                            item = item, isReviewed = true, isLoading = false, myReview = matchedReview, onSubmit = { _, _ -> }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun ReviewItemCard(item: OrderItem, isReviewed: Boolean, isLoading: Boolean, myReview: Review?, onSubmit: (Double, String) -> Unit) {
    var rating by remember { mutableStateOf(5) }
    var comment by remember { mutableStateOf("") }
    val orange600 = Color(0xFFEA580C)
    val grayBg = Color(0xFFF9FAFB)

    Card(
        colors = CardDefaults.cardColors(containerColor = if (isReviewed) grayBg else Color.White),
        elevation = CardDefaults.cardElevation(if (isReviewed) 0.dp else 2.dp),
        border = if (isReviewed) BorderStroke(1.dp, Color(0xFFE5E7EB)) else null,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            // Header Info sản phẩm
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(model = item.image, contentDescription = null, modifier = Modifier.size(50.dp).clip(RoundedCornerShape(4.dp)).background(Color(0xFFF3F4F6)), contentScale = ContentScale.Crop, alpha = if (isReviewed) 0.6f else 1f)
                Spacer(modifier = Modifier.width(12.dp))
                Text(item.name, fontSize = 14.sp, maxLines = 2, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Medium, color = if (isReviewed) Color.Gray else Color(0xFF1F2937))
            }

            if (isReviewed) {
                // ========================================================
                // GIAO DIỆN ĐÃ ĐÁNH GIÁ (XUẤT RA SỐ SAO + LỜI BÌNH CỦA KHÁCH)
                // ========================================================
                Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFE5E7EB))

                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
                    // Hiện số sao đã chấm (Khóa click)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Đánh giá: ", fontSize = 13.sp, color = Color.Gray)
                        val savedStars = myReview?.rating?.toInt() ?: 5
                        for (i in 1..5) {
                            Icon(
                                imageVector = Icons.Filled.Star, contentDescription = null,
                                tint = if (i <= savedStars) Color(0xFFF59E0B) else Color(0xFFD1D5DB),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Hiện nội dung bình luận đã lưu
                    val savedComment = myReview?.content ?: ""
                    Text(
                        text = if (savedComment.isNotBlank()) "Nội dung: \"$savedComment\"" else "Nội dung: Không có lời bình luận.",
                        fontSize = 13.sp,
                        color = Color(0xFF4B5563),
                        fontWeight = FontWeight.Normal
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Đã khóa đánh giá", color = Color(0xFF10B981), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            } else {
                // ========================================================
                // GIAO DIỆN CHƯA ĐÁNH GIÁ (FORM NHẬP LIỆU)
                // ========================================================
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    for (i in 1..5) {
                        Icon(
                            imageVector = if (i <= rating) Icons.Filled.Star else Icons.Outlined.StarBorder,
                            contentDescription = null,
                            tint = if (i <= rating) Color(0xFFF59E0B) else Color.Gray,
                            modifier = Modifier.size(44.dp).clickable(enabled = !isLoading) { rating = i }.padding(4.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = comment, onValueChange = { comment = it },
                    placeholder = { Text("Chia sẻ cảm nhận của bạn về sản phẩm...", fontSize = 13.sp) },
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    shape = RoundedCornerShape(8.dp),
                    enabled = !isLoading,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = orange600, unfocusedBorderColor = Color(0xFFD1D5DB))
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { onSubmit(rating.toDouble(), comment) },
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = orange600),
                    shape = RoundedCornerShape(8.dp),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text("Gửi đánh giá", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}