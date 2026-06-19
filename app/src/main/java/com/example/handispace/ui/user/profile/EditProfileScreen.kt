package com.example.handispace.ui.user.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.handispace.ui.components.AppTextField
import com.example.handispace.ui.components.AppTopBar
import com.example.handispace.ui.components.ErrorPopup
import com.example.handispace.ui.components.SuccessPopup
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun EditProfileScreen(navController: NavController, viewModel: ProfileViewModel) {
    val context = LocalContext.current // 🔥 Đã lấy Context thành công ở đây
    val state = viewModel.userState.value
    val user = state.data
    val coroutineScope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var avatarUrl by remember { mutableStateOf("") }

    // BIẾN QUẢN LÝ LỖI SỐ ĐIỆN THOẠI
    var phoneError by remember { mutableStateOf("") }

    // Quản lý trạng thái hiển thị Popup
    var showSuccessPopup by remember { mutableStateOf(false) }
    var showErrorPopup by remember { mutableStateOf(false) }
    var popupMessage by remember { mutableStateOf("") }

    val orange600 = Color(0xFFEA580C)
    val gray50 = Color(0xFFF9FAFB)
    val gray200 = Color(0xFFE5E7EB)
    val gray500 = Color(0xFF6B7280)

    LaunchedEffect(user) {
        if (user != null) {
            name = user.name
            phone = user.phone
            email = user.email
            avatarUrl = user.avatar_url
        }
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            avatarUrl = uri.toString()
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Sửa hồ sơ",
                onBackClick = { navController.popBackStack() }
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(16.dp)
            ) {
                Button(
                    onClick = {
                        // 1. Kiểm tra đầu vào
                        if (name.isBlank() || phone.isBlank()) {
                            popupMessage = "Vui lòng nhập đầy đủ Họ tên và Số điện thoại!"
                            showErrorPopup = true
                            return@Button
                        }

                        // 2. Lưu lên Firebase (🔥 Đã truyền thêm biến context vào đầu hàm)
                        viewModel.updateUserProfile(context, name, phone, avatarUrl) { isSuccess, msg ->
                            popupMessage = msg
                            if (isSuccess) {
                                showSuccessPopup = true
                                coroutineScope.launch {
                                    delay(1500)
                                    showSuccessPopup = false
                                    navController.popBackStack()
                                }
                            } else {
                                showErrorPopup = true
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = orange600),
                    shape = RoundedCornerShape(8.dp),
                    // KHÓA NÚT KHI ĐANG LƯU HOẶC KHI SỐ ĐIỆN THOẠI CÓ LỖI (CHƯA ĐỦ 10 SỐ)
                    enabled = !state.isLoading && phoneError.isEmpty() && phone.length == 10
                ) {
                    Text(if (state.isLoading) "ĐANG LƯU..." else "LƯU THAY ĐỔI", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        },
        containerColor = gray50
    ) { padding ->

        if (showSuccessPopup) {
            SuccessPopup(message = popupMessage, onDismiss = { showSuccessPopup = false })
        }
        if (showErrorPopup) {
            ErrorPopup(errorMessage = popupMessage, onDismiss = { showErrorPopup = false })
        }

        if (state.isLoading && !showSuccessPopup) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = orange600)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
            ) {
                // KHU VỰC ĐỔI AVATAR
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(modifier = Modifier.size(100.dp), contentAlignment = Alignment.BottomEnd) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(gray200),
                            contentAlignment = Alignment.Center
                        ) {
                            if (avatarUrl.isNotEmpty()) {
                                AsyncImage(
                                    model = avatarUrl,
                                    contentDescription = "Avatar",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Icon(Icons.Filled.Person, contentDescription = null, tint = gray500, modifier = Modifier.size(50.dp))
                            }
                        }

                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(orange600)
                                .border(2.dp, Color.White, CircleShape)
                                .clickable { launcher.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.CameraAlt, contentDescription = "Đổi ảnh", tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // KHU VỰC ĐIỀN THÔNG TIN
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(16.dp)
                ) {
                    AppTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = "Họ và Tên",
                        placeholder = "Nhập họ tên của bạn..."
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // ĐOẠN SỐ ĐIỆN THOẠI CÓ BẮT LỖI
                    AppTextField(
                        value = phone,
                        onValueChange = { input ->
                            if (input.all { it.isDigit() }) {
                                phone = input
                                phoneError = if (input.length != 10 && input.isNotEmpty()) {
                                    "Số điện thoại phải có đúng 10 chữ số"
                                } else {
                                    ""
                                }
                            }
                        },
                        label = "Số điện thoại",
                        placeholder = "Ví dụ: 0912345678",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    if (phoneError.isNotEmpty()) {
                        Text(
                            text = phoneError,
                            color = Color(0xFFEF4444),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = { },
                        label = { Text("Email (Không thể thay đổi)", color = gray500) },
                        enabled = false,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = gray500,
                            disabledBorderColor = gray200,
                            disabledLabelColor = gray500
                        )
                    )
                }
            }
        }
    }
}