package com.example.handispace.ui.admin.users

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Chat // 🔥 THÊM IMPORT ICON CHAT
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.handispace.navigation.Routes
import com.example.handispace.ui.components.AppTopBar
import com.example.handispace.ui.admin.orders.AdminOrderCard
import java.text.NumberFormat
import java.util.Locale

@Composable
fun AdminUserDetailScreen(
    navController: NavController,
    userId: String,
    viewModel: AdminUserViewModel = hiltViewModel()
) {
    val user = viewModel.usersState.value.data?.find { it.uid == userId }

    val userOrders = viewModel.specificUserOrders.value
    val isLoadingOrders = viewModel.isLoadingOrders.value

    val formatVND = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
    val orange600 = Color(0xFFEA580C)
    val gray50 = Color(0xFFF9FAFB)

    LaunchedEffect(userId) {
        viewModel.loadOrdersForUser(userId)
    }

    Scaffold(
        topBar = { AppTopBar(title = "Hồ sơ Khách hàng", onBackClick = { navController.popBackStack() }) },
        containerColor = gray50
    ) { padding ->
        if (user == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = orange600) }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
                // 1. HEADER THÔNG TIN KHÁCH HÀNG
                item {
                    Column(modifier = Modifier.fillMaxWidth().background(Color.White).padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        if (user.avatar_url.isNotBlank()) {
                            AsyncImage(model = user.avatar_url, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.size(80.dp).clip(CircleShape).background(gray50))
                        } else {
                            Box(modifier = Modifier.size(80.dp).clip(CircleShape).background(Color(0xFFE5E7EB)), contentAlignment = Alignment.Center) {
                                Icon(Icons.Filled.Person, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(40.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(user.name.ifBlank { "Chưa cập nhật tên" }, fontSize = 20.sp, fontWeight = FontWeight.Bold)

                        if (user.is_disabled) {
                            Text("TÀI KHOẢN ĐANG BỊ KHÓA", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp).background(Color.Red, RoundedCornerShape(4.dp)).padding(horizontal = 8.dp, vertical = 4.dp))
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Bảng thông số nhanh
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Tổng chi tiêu", fontSize = 13.sp, color = Color.Gray)
                                Text(formatVND.format(user.total_spent), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = orange600)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Hạng", fontSize = 13.sp, color = Color.Gray)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Star, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(16.dp))
                                    Text(user.loyalty_level.level_name, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937))
                                }
                            }
                        }
                    }
                    Divider(color = gray50, thickness = 8.dp)
                }

                // 2. LIÊN HỆ & NÚT CHAT
                item {
                    Column(modifier = Modifier.fillMaxWidth().background(Color.White).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Thông tin liên hệ", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Email, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(user.email, fontSize = 14.sp)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Phone, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(user.phone.ifBlank { "Chưa cập nhật số điện thoại" }, fontSize = 14.sp)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // 🔥 NÚT BẤM ĐỂ ADMIN CHỦ ĐỘNG NHẮN TIN CHO KHÁCH
                        Button(
                            onClick = {
                                val encodedName = android.net.Uri.encode(user.name.ifBlank { "Khách hàng" })
                                navController.navigate("chat/${user.uid}/$encodedName")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = orange600),
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Outlined.Chat, contentDescription = "Chat", tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Nhắn tin cho khách hàng", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                    Divider(color = gray50, thickness = 8.dp)
                }

                // 3. DANH SÁCH ĐƠN HÀNG CỦA USER NÀY
                item {
                    PaddingValues(horizontal = 16.dp, vertical = 12.dp).let {
                        Text("Lịch sử Đơn hàng (${userOrders.size})", fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp))
                    }
                }

                if (isLoadingOrders) {
                    item { Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = orange600) } }
                } else if (userOrders.isEmpty()) {
                    item { Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) { Text("Khách hàng này chưa có đơn hàng nào", color = Color.Gray) } }
                } else {
                    items(userOrders, key = { it.order_id }) { order ->
                        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                            AdminOrderCard(
                                order = order,
                                onClick = { navController.navigate("${Routes.ADMIN_ORDER_DETAIL}/${order.order_id}") },
                                onUpdateStatus = { newStatus -> viewModel.updateOrderStatus(order.order_id, newStatus, userId) }
                            )
                        }
                    }
                }
            }
        }
    }
}