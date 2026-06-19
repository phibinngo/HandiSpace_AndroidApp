package com.example.handispace.ui.user.order.history

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.handispace.model.Order
import com.example.handispace.navigation.Routes
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun OrderHistoryScreen(navController: NavController, viewModel: OrderHistoryViewModel) {
    val state = viewModel.ordersState.value
    val safeOrderList = state.data

    val orange600 = Color(0xFFEA580C)
    val gray50 = Color(0xFFF9FAFB)
    val formatVND = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))

    // 🔥 KHỞI TẠO BỘ FORMAT THỜI GIAN ĐỂ TRUYỀN VÀO THẺ ĐƠN HÀNG
    val sdf = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }

    val tabs = listOf(
        "all" to "Tất cả", "pending" to "Chờ xác nhận", "preparing" to "Đang chuẩn bị",
        "delivering" to "Đang giao", "completed" to "Đã giao", "return_pending" to "Chờ duyệt trả",
        "returned" to "Đã trả/Hoàn tiền", "cancelled" to "Đã hủy"
    )

    Column(modifier = Modifier.fillMaxSize().background(gray50)) {

        Surface(color = Color.White, shadowElevation = 1.dp) {
            Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                OutlinedTextField(
                    value = viewModel.searchQuery.value,
                    onValueChange = { viewModel.updateSearchQuery(it) },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    placeholder = { Text("Tìm tên sản phẩm đã mua...", fontSize = 14.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White, unfocusedContainerColor = Color.White,
                        focusedBorderColor = orange600, unfocusedBorderColor = Color(0xFFE5E7EB)
                    )
                )
            }
        }

        Surface(color = Color.White, shadowElevation = 2.dp) {
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                tabs.forEach { (key, label) ->
                    val isSelected = viewModel.selectedStatus.value == key
                    Box(
                        modifier = Modifier.clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) Color(0xFFFFF7ED) else Color(0xFFF3F4F6))
                            .border(1.dp, if (isSelected) orange600 else Color.Transparent, RoundedCornerShape(16.dp))
                            .clickable { viewModel.updateStatusFilter(key) }.padding(horizontal = 16.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(label, color = if (isSelected) orange600 else Color(0xFF4B5563), fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }

        if (safeOrderList != null) {
            if (safeOrderList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Chưa có đơn hàng nào ở trạng thái này.", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(safeOrderList) { order ->
                        // 🔥 TRUYỀN THÊM BIẾN sdf VÀO THẺ ĐƠN HÀNG
                        OrderCard(order = order, formatVND = formatVND, sdf = sdf) {
                            navController.navigate("${Routes.ORDER_DETAIL}/${order.order_id}")
                        }
                    }
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = orange600)
            }
        }
    }
}

// 🔥 BỔ SUNG THAM SỐ sdf VÀO ĐÂY
@Composable
fun OrderCard(order: Order, formatVND: NumberFormat, sdf: SimpleDateFormat, onClick: () -> Unit) {
    val statusText = when(order.status) {
        "pending" -> "CHỜ XÁC NHẬN"
        "preparing" -> "ĐANG CHUẨN BỊ"
        "delivering" -> "ĐANG GIAO HÀNG"
        "completed" -> "HOÀN THÀNH"
        "return_pending" -> "CHỜ DUYỆT TRẢ"
        "returned" -> "ĐÃ TRẢ HÀNG"
        "cancelled" -> "ĐÃ HỦY"
        else -> order.status.uppercase()
    }
    val statusColor = if (order.status == "cancelled" || order.status == "returned") Color.Red else if (order.status == "return_pending") Color(0xFFF59E0B) else Color(0xFFEA580C)

    val totalItems = order.order_items.sumOf { it.quantity }
    val firstItem = order.order_items.firstOrNull()

    // 🔥 LẤY CHUỖI THỜI GIAN
    val createTime = order.created_at?.toDate()?.let { sdf.format(it) } ?: ""

    Card(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp).clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(0.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {

            // 🔥 ĐÃ FIX LẠI HEADER: GÓC TRÁI LÀ THỜI GIAN, GÓC PHẢI LÀ TRẠNG THÁI
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(createTime, color = Color.Gray, fontSize = 12.sp)
                Text(statusText, color = statusColor, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }
            Divider(color = Color(0xFFF9FAFB), thickness = 1.dp)

            if (firstItem != null) {
                Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.Top) {
                    AsyncImage(
                        model = firstItem.image, contentDescription = null,
                        modifier = Modifier.size(70.dp).clip(RoundedCornerShape(4.dp)).background(Color(0xFFF3F4F6)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(firstItem.name, fontSize = 14.sp, maxLines = 2, overflow = TextOverflow.Ellipsis, color = Color(0xFF1F2937))
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Phân loại: Mặc định", fontSize = 12.sp, color = Color.Gray)
                            Text("x${firstItem.quantity}", fontSize = 12.sp, color = Color(0xFF1F2937))
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(formatVND.format(firstItem.price), color = Color(0xFF1F2937), fontSize = 14.sp, textAlign = TextAlign.End, modifier = Modifier.fillMaxWidth())
                    }
                }
            }

            if (order.order_items.size > 1) {
                Divider(color = Color(0xFFF9FAFB), thickness = 1.dp)
                Text(
                    "Xem thêm ${order.order_items.size - 1} sản phẩm ˅",
                    fontSize = 12.sp, color = Color.Gray,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    textAlign = TextAlign.Center
                )
            }

            Divider(color = Color(0xFFF9FAFB), thickness = 1.dp)

            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                Text("Thành tiền ($totalItems sản phẩm): ", fontSize = 13.sp, color = Color(0xFF1F2937))
                Text(formatVND.format(order.final_total), fontSize = 16.sp, color = Color(0xFFEA580C), fontWeight = FontWeight.Bold)
            }

            if (order.status in listOf("completed", "cancelled", "returned")) {
                Divider(color = Color(0xFFF9FAFB), thickness = 1.dp)
                Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.End) {
                    OutlinedButton(
                        onClick = { onClick() },
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(1.dp, Color(0xFFEA580C)),
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 0.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text("Mua lại", color = Color(0xFFEA580C), fontSize = 13.sp)
                    }
                }
            }
        }
    }
}