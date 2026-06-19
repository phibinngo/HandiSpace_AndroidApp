package com.example.handispace.ui.auth.forgot_password

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.handispace.ui.components.*

@Composable
fun ForgotPasswordScreen(
    navController: NavController,
    viewModel: ForgotPasswordViewModel
) {
    var taiKhoan by remember { mutableStateOf("") }
    val state = viewModel.forgotPasswordState.value
    val context = LocalContext.current

    LaunchedEffect(state.data) {
        if (state.data != null) {
            Toast.makeText(context, state.data, Toast.LENGTH_LONG).show()
            viewModel.resetState()
            navController.popBackStack()
        }
    }

    // Sử dụng AuthBackground chung
    AuthBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "KHÔI PHỤC MẬT KHẨU",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = orange600 // Dùng màu cam chủ đạo
            )
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Nhập Email, Số điện thoại hoặc Username để hệ thống tìm kiếm tài khoản và gửi link khôi phục.",
                fontSize = 14.sp,
                color = gray500, // Dùng màu xám chuẩn
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            Spacer(modifier = Modifier.height(32.dp))

            AuthTextField(
                value = taiKhoan,
                onValueChange = { taiKhoan = it },
                label = "Email / Số điện thoại / Username"
            )

            // Hiển thị lỗi
            if (!state.errorMessage.isNullOrEmpty()) {
                Text(
                    text = state.errorMessage!!,
                    color = Color.Red,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            } else {
                Spacer(modifier = Modifier.height(24.dp))
            }

            if (state.isLoading) {
                CircularProgressIndicator(color = orange600, modifier = Modifier.padding(16.dp))
            } else {
                AuthButton(
                    text = "GỬI YÊU CẦU",
                    onClick = {
                        if (taiKhoan.isBlank()) {
                            Toast.makeText(context, "Vui lòng nhập thông tin tài khoản!", Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.resetPassword(taiKhoan)
                        }
                    }
                    // Bỏ containerColor đi vì AuthButton đã có màu orange600 làm mặc định
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Link quay lại đăng nhập đồng bộ style
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(" Trở lại trang đăng nhập?", color = gray800)
                TextButton(
                    onClick = { navController.popBackStack() },
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    Text("Đăng nhập ngay", color = orange600, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}