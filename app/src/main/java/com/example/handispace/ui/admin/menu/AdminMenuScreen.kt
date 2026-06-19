package com.example.handispace.ui.admin.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.handispace.data.repository.user.UserRepository
import com.example.handispace.navigation.Routes
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

@Composable
fun AdminMenuScreen(
    navController: NavController
) {
    val orange600 = Color(0xFFEA580C)
    val gray50 = Color(0xFFF9FAFB)
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize().background(gray50).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        MenuCardItem(
            icon = Icons.Filled.Category,
            iconTint = orange600,
            title = "Quản lý Danh mục",
            subtitle = "Thêm, sửa, xóa các loại hàng hóa",
            onClick = { navController.navigate(Routes.ADMIN_CATEGORIES) }
        )

        // 🔥 ĐÃ FIX: Nối dây điều hướng sang trang Quản lý Voucher
        MenuCardItem(
            icon = Icons.Filled.ConfirmationNumber,
            iconTint = Color(0xFF10B981),
            title = "Quản lý Voucher",
            subtitle = "Tạo mã giảm giá, freeship cho khách",
            onClick = { navController.navigate(Routes.ADMIN_VOUCHERS) }
        )

        Card(
            modifier = Modifier.fillMaxWidth().clickable {
                coroutineScope.launch {
                    val userRepository = UserRepository(FirebaseAuth.getInstance(), FirebaseFirestore.getInstance())
                    userRepository.logout()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            },
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(2.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(48.dp).background(Color.Red.copy(alpha = 0.1f), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.ExitToApp, contentDescription = "Đăng xuất", tint = Color.Red)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text("Đăng xuất", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Red)
            }
        }
    }
}

@Composable
fun MenuCardItem(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier.size(48.dp).background(iconTint.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = iconTint)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(subtitle, fontSize = 13.sp, color = Color.Gray)
                }
            }
            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Color.Gray)
        }
    }
}