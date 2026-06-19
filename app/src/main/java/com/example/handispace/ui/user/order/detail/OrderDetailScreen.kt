package com.example.handispace.ui.user.order.detail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
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
import com.example.handispace.navigation.Routes
import com.example.handispace.ui.components.AppTopBar
import com.example.handispace.ui.components.ActionFormPopup
import com.example.handispace.ui.components.SuccessPopup
import com.example.handispace.ui.components.ErrorPopup
import com.example.handispace.ui.user.checkout.PaymentDetailRow
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun OrderDetailScreen(navController: NavController, orderId: String, viewModel: OrderDetailViewModel) {
    LaunchedEffect(orderId) { viewModel.getOrderDetail(orderId) }

    val state = viewModel.orderState.value
    val order = state.data
    val formatVND = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))

    // 🔥 BỔ SUNG BỘ FORMAT THỜI GIAN
    val sdf = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }

    val orange600 = Color(0xFFEA580C)
    val gray50 = Color(0xFFF9FAFB)

    var showCancelDialog by remember { mutableStateOf(false) }
    var showReturnDialog by remember { mutableStateOf(false) }

    if (viewModel.successMessage.value != null) {
        SuccessPopup(message = viewModel.successMessage.value ?: "", onDismiss = { viewModel.clearMessages() })
    }
    if (viewModel.errorMessage.value != null) {
        ErrorPopup(errorMessage = viewModel.errorMessage.value ?: "", onDismiss = { viewModel.clearMessages() })
    }

    if (showCancelDialog) {
        ActionFormPopup(
            title = "LÝ DO HỦY ĐƠN",
            placeholder = "Nhập lý do hủy đơn (bắt buộc)...",
            confirmText = "Xác nhận hủy",
            confirmColor = Color.Red,
            onConfirm = { reason ->
                viewModel.updateOrderStatusWithReason(orderId, "cancelled", reason)
                showCancelDialog = false
            },
            onCancel = { showCancelDialog = false }
        )
    }

    if (showReturnDialog) {
        ActionFormPopup(
            title = "YÊU CẦU TRẢ HÀNG",
            placeholder = "Mô tả lý do/Tình trạng hàng lỗi...",
            confirmText = "Gửi Yêu Cầu",
            confirmColor = orange600,
            onConfirm = { reason ->
                viewModel.updateOrderStatusWithReason(orderId, "return_pending", reason)
                showReturnDialog = false
            },
            onCancel = { showReturnDialog = false }
        )
    }

    Scaffold(
        topBar = { AppTopBar(title = "Thông tin chi tiết đơn mua", onBackClick = { navController.popBackStack() }) },
        bottomBar = {
            if (order != null) {
                Surface(shadowElevation = 8.dp) {
                    Column(modifier = Modifier.fillMaxWidth().background(Color.White).padding(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = {
                                    val adminId = "axTeqn5PxRet3M5vYr1nvgbnp542"
                                    val adminName = android.net.Uri.encode("CSKH HandiSpace")
                                    val msg = android.net.Uri.encode("Xin chào, tôi cần hỗ trợ về đơn hàng mã: ${order.order_id}")

                                    navController.navigate("chat/$adminId/$adminName?context=$msg")
                                },
                                modifier = Modifier.weight(1f).height(44.dp),
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, orange600)
                            ) {
                                Text("Liên hệ Admin ", color = orange600, fontSize = 13.sp)
                            }
                            when (order.status) {
                                "pending" -> {
                                    Button(onClick = { showCancelDialog = true }, modifier = Modifier.weight(1f).height(44.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.Red), shape = RoundedCornerShape(6.dp)) { Text("Hủy đơn", color = Color.White, fontSize = 13.sp) }
                                }
                                "delivering" -> {
                                    Button(onClick = { viewModel.updateOrderStatusWithReason(orderId, "completed") }, modifier = Modifier.weight(1f).height(44.dp), colors = ButtonDefaults.buttonColors(containerColor = orange600), shape = RoundedCornerShape(6.dp)) { Text("Đã nhận hàng", color = Color.White, fontSize = 13.sp) }
                                }
                                "completed", "cancelled", "returned" -> {
                                    Button(onClick = { viewModel.buyAgain(order) }, modifier = Modifier.weight(1f).height(44.dp), colors = ButtonDefaults.buttonColors(containerColor = orange600), shape = RoundedCornerShape(6.dp)) { Text("Mua lại đơn", color = Color.White, fontSize = 13.sp) }
                                }
                            }
                        }

                        if (order.status == "completed") {
                            val isAllReviewed = order.order_items.isNotEmpty() && order.order_items.all { it.is_reviewed }

                            Spacer(modifier = Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(onClick = { showReturnDialog = true }, modifier = Modifier.weight(1f).height(44.dp), shape = RoundedCornerShape(6.dp), border = BorderStroke(1.dp, Color.Gray)) { Text("Trả hàng/Hoàn tiền", color = Color.DarkGray, fontSize = 13.sp) }

                                Button(
                                    onClick = { navController.navigate("${Routes.REVIEW_ORDER}/${order.order_id}") },
                                    modifier = Modifier.weight(1f).height(44.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = if (isAllReviewed) Color.White else orange600),
                                    shape = RoundedCornerShape(6.dp),
                                    border = if (isAllReviewed) BorderStroke(1.dp, orange600) else null
                                ) {
                                    Text(
                                        text = if (isAllReviewed) "Xem lại đánh giá" else "Đánh giá sản phẩm",
                                        color = if (isAllReviewed) orange600 else Color.White,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        containerColor = gray50
    ) { padding ->
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = orange600) }
        } else if (order != null) {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
                item {
                    val (bannerMsg, bannerBg) = when(order.status) {
                        "pending" -> "Đơn hàng đang chờ người bán xác nhận" to Color(0xFF3B82F6)
                        "preparing" -> "Người bán đang đóng gói kiện hàng" to Color(0xFFF59E0B)
                        "delivering" -> "Đơn hàng đang được shipper giao đến bạn" to Color(0xFF0FA49C)
                        "completed" -> "Đơn hàng đã được giao nhận thành công" to Color(0xFF10B981)
                        "return_pending" -> "Yêu cầu trả hàng của bạn đang chờ Admin duyệt" to Color(0xFF6B7280)
                        "returned" -> "Đã hoàn tất thủ tục hoàn tiền/trả hàng" to Color(0xFFEF4444)
                        "cancelled" -> "Đơn hàng đã bị hủy bỏ thành công" to Color.Red
                        else -> "" to Color.Gray
                    }
                    Box(modifier = Modifier.fillMaxWidth().background(bannerBg).padding(16.dp)) {
                        Text(bannerMsg, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                }

                item {
                    Column(modifier = Modifier.fillMaxWidth().background(Color.White).padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = orange600, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Địa chỉ nhận hàng", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937))
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("${order.shipping_info.name} | ${order.shipping_info.phone}", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Text(order.shipping_info.address, fontSize = 13.sp, color = Color.Gray)

                        // 🔥 ĐÃ THÊM: Chèn hiển thị lời nhắn để User tự xem lại được ghi chú của mình
                        if (order.note.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Lời nhắn: ${order.note}", fontSize = 13.sp, color = orange600, fontWeight = FontWeight.Medium)
                        }
                    }
                    Divider(color = gray50, thickness = 8.dp)
                }

                items(order.order_items) { item ->
                    Row(modifier = Modifier.fillMaxWidth().background(Color.White).padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(model = item.image, contentDescription = null, modifier = Modifier.size(60.dp).clip(RoundedCornerShape(4.dp)).background(gray50), contentScale = ContentScale.Crop)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.name, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(formatVND.format(item.price), color = orange600, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                Text("x${item.quantity}", fontSize = 14.sp)
                            }
                        }
                    }
                    Divider(color = gray50, thickness = 1.dp)
                }

                // ... (Giữ nguyên phần import và code phía trên) ...

                item {
                    Column(modifier = Modifier.background(Color.White).padding(16.dp)) {
                        Text("Chi tiết biểu phí thanh toán", fontSize = 15.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))
                        PaymentDetailRow("Tổng tiền hàng gốc", formatVND.format(order.subtotal))

                        // 🔥 TÍNH TOÁN NGƯỢC HẠNG VIP VÀ HIỂN THỊ CHI TIẾT
                        if (order.member_discount > 0 && order.subtotal > 0) {
                            val percent = Math.round((order.member_discount / order.subtotal) * 100.0).toInt()
                            val rankName = when(percent) { 8 -> "Kim Cương"; 5 -> "Vàng"; 2 -> "Bạc"; else -> "VIP" }
                            PaymentDetailRow("Ưu đãi Thành viên $rankName ($percent%)", "-${formatVND.format(order.member_discount)}", orange600)
                        }

                        if (order.order_discount > 0) PaymentDetailRow("Mã voucher giảm giá đơn hàng", "-${formatVND.format(order.order_discount)}", orange600)
                        PaymentDetailRow("Chi phí vận chuyển", formatVND.format(order.shipping_fee))
                        if (order.freeship_discount > 0) PaymentDetailRow("Mã freeship giảm giá vận chuyển", "-${formatVND.format(order.freeship_discount)}", Color(0xFF0FA49C))

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Tổng số thực thu", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937))
                            Text(formatVND.format(order.final_total), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = orange600)
                        }
                    }
                    Divider(color = gray50, thickness = 8.dp)


                    Column(modifier = Modifier.fillMaxWidth().background(Color.White).padding(16.dp)) {
                        Text("Mã vận đơn: ${order.order_id.uppercase()}", fontSize = 12.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Hình thức: ${if (order.payment_method == "COD") "Thanh toán tiền mặt khi giao hàng" else "Chuyển khoản liên ngân hàng"}", fontSize = 12.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(4.dp))

                        // 🔥 ĐÃ BỔ SUNG HIỂN THỊ NGÀY ĐẶT HÀNG BÊN USER
                        val createTime = order.created_at?.toDate()?.let { sdf.format(it) } ?: "Đang cập nhật"
                        Text("Ngày đặt hàng: $createTime", fontSize = 12.sp, color = Color.Gray)
                    }
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }
}