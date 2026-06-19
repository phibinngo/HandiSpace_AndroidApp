package com.example.handispace.ui.admin.users

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
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
import com.example.handispace.model.User
import com.example.handispace.navigation.Routes
import com.example.handispace.ui.components.SuccessPopup
import com.example.handispace.ui.components.ErrorPopup
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUsersScreen(
    navController: NavController,
    viewModel: AdminUserViewModel = hiltViewModel()
) {
    val state = viewModel.usersState.value
    val safeUserList = state.data

    val orange600 = Color(0xFFEA580C)
    val gray50 = Color(0xFFF9FAFB)
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    var userToConfirm by remember { mutableStateOf<User?>(null) }

    // ĐÓN STATE TIN NHẮN ĐỂ HIỂN THỊ POPUPS
    if (viewModel.successMessage.value != null) {
        SuccessPopup(message = viewModel.successMessage.value ?: "", onDismiss = { viewModel.clearMessages() })
    }
    if (viewModel.errorMessage.value != null) {
        ErrorPopup(errorMessage = viewModel.errorMessage.value ?: "", onDismiss = { viewModel.clearMessages() })
    }

    // POPUP XÁC NHẬN TRƯỚC KHI THỰC HIỆN KHÓA/MỞ
    if (userToConfirm != null) {
        val isLocking = !userToConfirm!!.is_disabled
        val title = if (isLocking) "Vô hiệu hóa tài khoản?" else "Kích hoạt lại tài khoản?"
        val text = if (isLocking) {
            "Bạn có chắc chắn muốn khóa tài khoản của khách hàng ${userToConfirm!!.name} không?"
        } else {
            "Bạn có chắc chắn muốn mở khóa và kích hoạt lại tài khoản của khách hàng ${userToConfirm!!.name} không?"
        }
        val confirmButtonColor = if (isLocking) Color.Red else Color(0xFF10B981)

        AlertDialog(
            onDismissRequest = { userToConfirm = null },
            title = { Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
            text = { Text(text, fontSize = 14.sp) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.toggleUserLock(userToConfirm!!)
                        userToConfirm = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = confirmButtonColor)
                ) { Text("Xác nhận", color = Color.White, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                OutlinedButton(onClick = { userToConfirm = null }) { Text("Hủy bỏ", color = Color.Gray) }
            },
            containerColor = Color.White
        )
    }

    Column(modifier = Modifier.fillMaxSize().background(gray50)) {
        Box(modifier = Modifier.fillMaxWidth().background(Color.White).padding(16.dp)) {
            OutlinedTextField(
                value = viewModel.searchQuery.value,
                onValueChange = {
                    viewModel.searchQuery.value = it
                    viewModel.applyFilters()
                },
                placeholder = { Text("Tìm tên, email, sđt...", color = Color.Gray) },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = orange600) },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = orange600,
                    unfocusedBorderColor = Color(0xFFE5E7EB),
                    focusedContainerColor = gray50,
                    unfocusedContainerColor = gray50
                )
            )
        }

        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = orange600)
            }
        } else if (safeUserList != null) {
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                Text("Tổng: ${safeUserList.size} khách hàng", fontWeight = FontWeight.Bold, color = Color.Gray)
            }

            if (safeUserList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Không tìm thấy khách hàng", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(safeUserList, key = { it.uid }) { user ->
                        Card(
                            modifier = Modifier.fillMaxWidth().clickable { navController.navigate("${Routes.ADMIN_USER_DETAIL}/${user.uid}") },
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                if (user.avatar_url.isNotBlank()) {
                                    AsyncImage(model = user.avatar_url, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.size(50.dp).clip(CircleShape).background(gray50))
                                } else {
                                    Box(modifier = Modifier.size(50.dp).clip(CircleShape).background(Color(0xFFE5E7EB)), contentAlignment = Alignment.Center) {
                                        Icon(Icons.Filled.Person, contentDescription = null, tint = Color.Gray)
                                    }
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(user.name.ifBlank { "Chưa cập nhật tên" }, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                    Text(user.email, fontSize = 13.sp, color = Color.Gray)
                                    val joinDate = user.created_at?.toDate()?.let { sdf.format(it) } ?: "N/A"
                                    Text("Tham gia: $joinDate", fontSize = 12.sp, color = orange600)
                                }

                                // 🔥 ICON TỰ ĐỘNG ĐỔI MÀU DỰA TRÊN STATE HIỆN TẠI
                                IconButton(
                                    onClick = { userToConfirm = user },
                                    modifier = Modifier.size(40.dp).background(if (user.is_disabled) Color.Red.copy(alpha = 0.1f) else Color(0xFF10B981).copy(alpha = 0.1f), CircleShape)
                                ) {
                                    Icon(
                                        imageVector = if (user.is_disabled) Icons.Filled.Lock else Icons.Filled.LockOpen,
                                        contentDescription = "Lock Status",
                                        tint = if (user.is_disabled) Color.Red else Color(0xFF10B981)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}