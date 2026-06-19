package com.example.handispace.ui.admin.orders

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Warning
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.handispace.ui.components.AppTopBar
import com.example.handispace.ui.user.checkout.PaymentDetailRow
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun AdminOrderDetailScreen(
    navController: NavController,
    orderId: String,
    viewModel: AdminOrderViewModel = hiltViewModel()
) {
    val order = viewModel.ordersState.value.data?.find { it.order_id == orderId }

    val formatVND = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    val orange600 = Color(0xFFEA580C)
    val gray50 = Color(0xFFF9FAFB)

    var showDropdown by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { AppTopBar(title = "Chi tiết đơn hàng Admin", onBackClick = { navController.popBackStack() }) },
        bottomBar = {
            if (order != null) {
                Surface(shadowElevation = 8.dp) {
                    Column(modifier = Modifier.fillMaxWidth().background(Color.White).padding(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {

                            Box {
                                OutlinedButton(
                                    onClick = { showDropdown = true },
                                    modifier = Modifier.height(44.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp)
                                ) {
                                    Text("Đổi trạng thái", fontSize = 12.sp, color = Color.Gray)
                                    Icon(Icons.Filled.ArrowDropDown, contentDescription = null, tint = Color.Gray)
                                }
                                DropdownMenu(expanded = showDropdown, onDismissRequest = { showDropdown = false }) {
                                    val statusList = listOf(
                                        "pending" to "Chờ xác nhận", "preparing" to "Đang xử lý",
                                        "delivering" to "Đang giao", "completed" to "Hoàn thành",
                                        "cancelled" to "Hủy đơn"
                                    )
                                    statusList.forEach { (code, label) ->
                                        DropdownMenuItem(
                                            text = { Text(label, fontSize = 14.sp) },
                                            onClick = { viewModel.updateStatus(orderId, code); showDropdown = false }
                                        )
                                    }
                                }
                            }

                            Box(modifier = Modifier.weight(1f, fill = false), contentAlignment = Alignment.CenterEnd) {
                                when (order.status) {
                                    "pending" -> {
                                        Button(onClick = { viewModel.updateStatus(orderId, "preparing") }, colors = ButtonDefaults.buttonColors(containerColor = orange600), modifier = Modifier.height(44.dp), shape = RoundedCornerShape(8.dp)) {
                                            Text("Duyệt Đơn Hàng", fontSize = 13.sp)
                                        }
                                    }
                                    "preparing" -> {
                                        Button(onClick = { viewModel.updateStatus(orderId, "delivering") }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0FA49C)), modifier = Modifier.height(44.dp), shape = RoundedCornerShape(8.dp)) {
                                            Text("Giao Shipper", fontSize = 13.sp)
                                        }
                                    }
                                    "return_pending" -> {
                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            OutlinedButton(onClick = { viewModel.updateStatus(orderId, "completed") }, modifier = Modifier.height(44.dp), shape = RoundedCornerShape(8.dp), contentPadding = PaddingValues(horizontal = 8.dp)) {
                                                Text("Từ chối", color = Color.Gray, fontSize = 12.sp)
                                            }
                                            Button(onClick = { viewModel.updateStatus(orderId, "returned") }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red), modifier = Modifier.height(44.dp), shape = RoundedCornerShape(8.dp), contentPadding = PaddingValues(horizontal = 8.dp)) {
                                                Text("Đồng ý", fontSize = 12.sp, maxLines = 1)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        containerColor = gray50
    ) { padding ->
        if (order == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = orange600) }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {

                item {
                    val (bannerMsg, bannerBg) = when(order.status) {
                        "pending" -> "CHỜ XÁC NHẬN" to Color(0xFF3B82F6)
                        "preparing" -> "ĐANG XỬ LÝ (ĐÓNG GÓI)" to Color(0xFFF59E0B)
                        "delivering" -> "ĐANG TRÊN ĐƯỜNG GIAO" to Color(0xFF0FA49C)
                        "completed" -> "ĐÃ GIAO THÀNH CÔNG" to Color(0xFF10B981)
                        "return_pending" -> "YÊU CẦU HOÀN TRẢ" to Color(0xFFEC4899)
                        "returned" -> "ĐÃ CHẤP NHẬN TRẢ HÀNG" to Color.Red
                        "cancelled" -> "ĐƠN BỊ HỦY" to Color.Gray
                        else -> "KHÔNG RÕ" to Color.Black
                    }
                    Box(modifier = Modifier.fillMaxWidth().background(bannerBg).padding(vertical = 16.dp, horizontal = 20.dp)) {
                        Text(bannerMsg, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // 🔥 ĐÃ FIX LẠI CÁCH BẮT LÝ DO HỦY VÀ TRẢ HÀNG
                item {
                    val reasonText = order.return_reason.takeIf { !it.isNullOrBlank() }
                        ?: order.cancel_reason.takeIf { !it.isNullOrBlank() }
                        ?: order.note

                    if ((order.status == "return_pending" || order.status == "cancelled" || order.status == "returned") && reasonText.isNotBlank()) {
                        Row(modifier = Modifier.fillMaxWidth().background(Color(0xFFFEF2F2)).padding(16.dp), verticalAlignment = Alignment.Top) {
                            Icon(Icons.Filled.Warning, contentDescription = null, tint = Color.Red, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                val title = if (order.status == "cancelled") "Lý do hủy đơn:" else "Lý do trả hàng:"
                                Text(title, color = Color.Red, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(reasonText, color = Color.Red, fontSize = 14.sp)
                            }
                        }
                    }
                }

                item {
                    Column(modifier = Modifier.fillMaxWidth().background(Color.White).padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = orange600, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Thông tin Khách hàng", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937))
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("${order.shipping_info.name} | ${order.shipping_info.phone}", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Text(order.shipping_info.address, fontSize = 14.sp, color = Color.Gray, modifier = Modifier.padding(top = 4.dp))

                        // 🔥 ĐÃ SỬA: Bỏ điều kiện status, lời nhắn của khách sẽ hiện xuyên suốt mọi trạng thái đơn
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
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(formatVND.format(item.price), color = orange600, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                Text("x${item.quantity}", fontSize = 14.sp)
                            }
                        }
                    }
                    Divider(color = gray50, thickness = 1.dp)
                }

                item {
                    Column(modifier = Modifier.background(Color.White).padding(16.dp)) {
                        Text("Chi tiết doanh thu", fontSize = 15.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))
                        PaymentDetailRow("Tiền hàng khách mua", formatVND.format(order.subtotal))

                        if (order.member_discount > 0 && order.subtotal > 0) {
                            val percent = Math.round((order.member_discount / order.subtotal) * 100.0).toInt()
                            val rankName = when(percent) { 8 -> "Kim Cương"; 5 -> "Vàng"; 2 -> "Bạc"; else -> "VIP" }
                            PaymentDetailRow("Trừ ưu đãi Thành viên $rankName ($percent%)", "-${formatVND.format(order.member_discount)}", orange600)
                        }

                        if (order.order_discount > 0) PaymentDetailRow("Trừ Voucher Đơn hàng", "-${formatVND.format(order.order_discount)}", orange600)
                        PaymentDetailRow("Phí vận chuyển", formatVND.format(order.shipping_fee))
                        if (order.freeship_discount > 0) PaymentDetailRow("Trừ Voucher Freeship", "-${formatVND.format(order.freeship_discount)}", Color(0xFF0FA49C))

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("THỰC THU KHÁCH HÀNG", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937))
                            Text(formatVND.format(order.final_total), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = orange600)
                        }
                    }
                    Divider(color = gray50, thickness = 8.dp)

                    Column(modifier = Modifier.fillMaxWidth().background(Color.White).padding(16.dp)) {
                        Text("Mã vận đơn: ${order.order_id.uppercase()}", fontSize = 13.sp, color = Color(0xFF4B5563))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Thanh toán: ${if (order.payment_method == "COD") "Tiền mặt (COD)" else "Chuyển khoản"}", fontSize = 13.sp, color = Color(0xFF4B5563))
                        Spacer(modifier = Modifier.height(4.dp))
                        val createTime = order.created_at?.toDate()?.let { sdf.format(it) } ?: "Đang cập nhật"
                        Text("Ngày đặt: $createTime", fontSize = 13.sp, color = Color.Gray)

                        val updateTime = order.updated_at?.toDate()?.let { sdf.format(it) } ?: "Đang cập nhật"
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Cập nhật cuối: $updateTime", fontSize = 13.sp, color = Color.Gray)
                    }
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }
}