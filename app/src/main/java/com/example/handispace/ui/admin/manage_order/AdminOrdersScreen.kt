package com.example.handispace.ui.admin.orders

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Assignment
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
import com.example.handispace.model.Order
import com.example.handispace.navigation.Routes
import java.text.NumberFormat
import java.util.Locale

@Composable
fun AdminOrdersScreen(
    navController: NavController,
    viewModel: AdminOrderViewModel = hiltViewModel()
) {
    val selectedTab = viewModel.selectedTab.value
    val filteredOrders = viewModel.getFilteredOrders()
    val isLoading = viewModel.isLoading.value

    val orange600 = Color(0xFFEA580C)
    val gray50 = Color(0xFFF9FAFB)

    Column(modifier = Modifier.fillMaxSize().background(gray50)) {
        ScrollableTabRow(
            selectedTabIndex = viewModel.tabs.indexOf(selectedTab).coerceAtLeast(0),
            containerColor = Color.White,
            contentColor = orange600,
            edgePadding = 8.dp,
            indicator = { tabPositions ->
                if (tabPositions.isNotEmpty()) {
                    TabRowDefaults.Indicator(
                        Modifier.tabIndicatorOffset(tabPositions[viewModel.tabs.indexOf(selectedTab).coerceAtLeast(0)]),
                        color = orange600,
                        height = 3.dp
                    )
                }
            }
        ) {
            viewModel.tabs.forEach { tabName ->
                Tab(
                    selected = selectedTab == tabName,
                    onClick = { viewModel.updateStatusFilter(tabName) },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = tabName,
                                fontWeight = if (selectedTab == tabName) FontWeight.Bold else FontWeight.Medium,
                                color = if (selectedTab == tabName) orange600 else Color.Gray
                            )

                            // Hiển thị Bubble thông báo đỏ
                            val badgeCount = when (tabName) {
                                "Chờ xác nhận" -> viewModel.pendingCount.value
                                "Yêu cầu trả" -> viewModel.returnCount.value
                                else -> 0
                            }
                            if (badgeCount > 0) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Box(
                                    modifier = Modifier.background(Color.Red, CircleShape).padding(horizontal = 6.dp, vertical = 2.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = badgeCount.toString(), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                )
            }
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = orange600) }
        } else if (filteredOrders.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Không có đơn hàng nào", color = Color.Gray) }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(filteredOrders, key = { it.order_id }) { order ->
                    AdminOrderCard(
                        order = order,
                        onClick = { navController.navigate("${Routes.ADMIN_ORDER_DETAIL}/${order.order_id}") },
                        onUpdateStatus = { newStatus -> viewModel.updateStatus(order.order_id, newStatus) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminOrderCard(order: Order, onClick: () -> Unit, onUpdateStatus: (String) -> Unit) {
    val formatVND = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
    val orange600 = Color(0xFFEA580C)
    var showDropdown by remember { mutableStateOf(false) }

    val (statusText, statusColor) = when(order.status) {
        "pending" -> "Chờ xác nhận" to Color(0xFF3B82F6)
        "preparing" -> "Đang xử lý" to Color(0xFFF59E0B)
        "delivering" -> "Đang giao" to Color(0xFF0FA49C)
        "completed" -> "Hoàn thành" to Color(0xFF10B981)
        "return_pending" -> "Yêu cầu Trả hàng" to Color(0xFFEC4899)
        "returned" -> "Đã Trả hàng" to Color.Red
        "cancelled" -> "Đã Hủy" to Color.Gray
        else -> "Không rõ" to Color.Black
    }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Assignment, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(order.order_id.takeLast(8).uppercase(), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Text(statusText, color = statusColor, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
            Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFF3F4F6))

            Text("Khách: ${order.shipping_info.name} - ${order.shipping_info.phone}", fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Text(order.shipping_info.address, fontSize = 13.sp, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(modifier = Modifier.height(12.dp))

            if (order.order_items.isNotEmpty()) {
                val firstItem = order.order_items.first()
                Row(modifier = Modifier.fillMaxWidth().background(Color(0xFFF9FAFB), RoundedCornerShape(8.dp)).padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(model = firstItem.image, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.size(40.dp).clip(RoundedCornerShape(4.dp)))
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(firstItem.name, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text("x${firstItem.quantity}", fontSize = 12.sp, color = Color.Gray)
                    }
                }
            }
            if (order.order_items.size > 1) {
                Text("... và ${order.order_items.size - 1} sản phẩm khác", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(top = 4.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))

            // 🔥 HIỂN THỊ RÕ RÀNG LÝ DO TRẢ HOẶC LÝ DO HỦY TRÊN CARD
            val reasonText = order.return_reason.takeIf { !it.isNullOrBlank() }
                ?: order.cancel_reason.takeIf { !it.isNullOrBlank() }
                ?: order.note

            if ((order.status == "return_pending" || order.status == "cancelled" || order.status == "returned") && reasonText.isNotBlank()) {
                Row(modifier = Modifier.fillMaxWidth().background(Color(0xFFFEF2F2), RoundedCornerShape(8.dp)).padding(8.dp)) {
                    Icon(Icons.Filled.Warning, contentDescription = null, tint = Color.Red, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    val title = if (order.status == "cancelled") "Lý do hủy:" else "Lý do trả:"
                    Text("$title $reasonText", color = Color.Red, fontSize = 13.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Tổng thanh toán:", fontSize = 14.sp)
                Text(formatVND.format(order.final_total), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = orange600)
            }
            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Box {
                    OutlinedButton(onClick = { showDropdown = true }, contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp), modifier = Modifier.height(36.dp)) {
                        Text("Đổi trạng thái", fontSize = 11.sp, color = Color.Gray)
                        Icon(Icons.Filled.ArrowDropDown, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                    }
                    DropdownMenu(expanded = showDropdown, onDismissRequest = { showDropdown = false }) {
                        val statusList = listOf("pending" to "Chờ xác nhận", "preparing" to "Đang xử lý", "delivering" to "Đang giao", "completed" to "Hoàn thành", "cancelled" to "Hủy đơn")
                        statusList.forEach { (code, label) -> DropdownMenuItem(text = { Text(label, fontSize = 14.sp) }, onClick = { onUpdateStatus(code); showDropdown = false }) }
                    }
                }

                // Căn chỉnh chữ gọn gàng
                when (order.status) {
                    "pending" -> Button(onClick = { onUpdateStatus("preparing") }, colors = ButtonDefaults.buttonColors(containerColor = orange600), modifier = Modifier.height(36.dp)) { Text("Duyệt Đơn", fontSize = 12.sp) }
                    "preparing" -> Button(onClick = { onUpdateStatus("delivering") }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0FA49C)), modifier = Modifier.height(36.dp)) { Text("Giao Shipper", fontSize = 12.sp) }
                    "return_pending" -> Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedButton(onClick = { onUpdateStatus("completed") }, modifier = Modifier.height(36.dp), contentPadding = PaddingValues(horizontal = 8.dp)) {
                            Text("Từ chối", color = Color.Gray, fontSize = 12.sp)
                        }
                        Button(onClick = { onUpdateStatus("returned") }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red), modifier = Modifier.height(36.dp), contentPadding = PaddingValues(horizontal = 8.dp)) {
                            Text("Đồng ý", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}