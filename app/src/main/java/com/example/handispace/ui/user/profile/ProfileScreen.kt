package com.example.handispace.ui.user.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.handispace.navigation.Routes
// 🔥 IMPORT THÊM ĐỂ ĐẾM SỐ TIN NHẮN
import com.example.handispace.ui.chat.ChatListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController, viewModel: ProfileViewModel) {
    val state = viewModel.userState.value
    val user = state.data

    val orange600 = Color(0xFFEA580C)
    val gray50 = Color(0xFFF9FAFB)
    val gray100 = Color(0xFFF3F4F6)
    val gray500 = Color(0xFF6B7280)
    val gray800 = Color(0xFF1F2937)

    // 🔥 GỌI VIEWMODEL CHAT ĐỂ LẤY SỐ TIN NHẮN CHƯA ĐỌC
    val chatListViewModel: ChatListViewModel = hiltViewModel()
    val unreadChatCount = chatListViewModel.totalUnreadCount.value

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(gray100)
                            .clickable { navController.navigate(Routes.SEARCH) }
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Search, contentDescription = "Tìm", tint = Color.Gray)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Tìm kiếm trong HandiSpace...", color = Color.Gray, fontSize = 14.sp)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val adminId = "axTeqn5PxRet3M5vYr1nvgbnp542" // 🔥 ĐÃ ĐỔI SANG UID XỊN
                        val adminName = android.net.Uri.encode("CSKH HandiSpace")
                        navController.navigate("chat/$adminId/$adminName")
                    }) {
                        // 🔥 HIỆN CHẤM ĐỎ (BADGE) NẾU CÓ TIN NHẮN MỚI
                        if (unreadChatCount > 0) {
                            BadgedBox(badge = { Badge(containerColor = Color.Red) { Text(unreadChatCount.toString(), color = Color.White) } }) {
                                Icon(Icons.Outlined.Chat, contentDescription = "Chat", tint = orange600)
                            }
                        } else {
                            Icon(Icons.Outlined.Chat, contentDescription = "Chat", tint = orange600)
                        }
                    }
                    IconButton(onClick = { navController.navigate(Routes.CART) }) {
                        Icon(Icons.Outlined.ShoppingCart, contentDescription = "Giỏ hàng", tint = orange600)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = gray50
    ) { padding ->

        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = orange600) }
        } else if (user != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(gray100),
                        contentAlignment = Alignment.Center
                    ) {
                        if (user.avatar_url.isNotEmpty()) {
                            AsyncImage(
                                model = user.avatar_url,
                                contentDescription = "Avatar",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            // 🔥 ĐÃ SỬA: Thay icon xám xịt thành bản mặt của ní ở màn hình Profile chính
                            AsyncImage(
                                model = "https://res.cloudinary.com/demo/image/upload/w_200,h_200,c_fill,g_face,r_max/v1/fl_a_face.jpg",
                                contentDescription = "Mặt của tao",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = user.name.ifEmpty { "Người dùng HandiSpace" }, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = gray800)
                    Text(text = user.email, fontSize = 14.sp, color = gray500)

                    Spacer(modifier = Modifier.height(8.dp))

                    val currentRankName = user.loyalty_level.level_name
                    val iconRank = when (currentRankName) {
                        "Kim Cương" -> "💎"
                        "Vàng" -> "🥇"
                        "Bạc" -> "🥈"
                        else -> "🥉"
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFFFF7ED))
                            .border(1.dp, orange600, RoundedCornerShape(16.dp))
                            .clickable { navController.navigate("loyalty_screen") }
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("$iconRank Hạng: $currentRankName", color = orange600, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Filled.KeyboardArrowRight, contentDescription = null, tint = orange600, modifier = Modifier.size(16.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Column(modifier = Modifier.background(Color.White)) {
                    ProfileMenuItem(
                        icon = Icons.Filled.PersonOutline,
                        title = "Quản lý thông tin cá nhân",
                        onClick = { navController.navigate(Routes.EDIT_PROFILE) }
                    )
                    Divider(color = gray50, thickness = 1.dp, modifier = Modifier.padding(start = 48.dp))

                    ProfileMenuItem(
                        icon = Icons.Filled.LocationOn,
                        title = "Quản lý địa chỉ giao hàng",
                        onClick = { navController.navigate(Routes.ADDRESS) }
                    )
                    Divider(color = gray50, thickness = 1.dp, modifier = Modifier.padding(start = 48.dp))

                    ProfileMenuItem(
                        icon = Icons.Filled.LocalOffer,
                        title = "Kho Voucher của tôi",
                        onClick = { navController.navigate(Routes.MY_VOUCHER) }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Column(modifier = Modifier.background(Color.White)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.logout()
                                navController.navigate(Routes.LOGIN) {
                                    popUpTo(0)
                                }
                            }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text("Đăng xuất", color = Color.Red, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Lỗi tải hồ sơ") }
        }
    }
}

@Composable
fun ProfileMenuItem(icon: ImageVector, title: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = Color(0xFFEA580C), modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = title, fontSize = 15.sp, color = Color(0xFF1F2937), modifier = Modifier.weight(1f))
        Icon(imageVector = Icons.Filled.KeyboardArrowRight, contentDescription = "Xem", tint = Color.Gray)
    }
}