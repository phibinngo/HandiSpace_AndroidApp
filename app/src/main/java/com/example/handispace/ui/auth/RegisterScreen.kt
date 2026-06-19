package com.example.handispace.ui.auth

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.handispace.ui.components.*

@Composable
fun RegisterScreen(
    navController: NavController,
    viewModel: AuthViewModel
) {
    var name by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var phoneError by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf("") }

    val state = viewModel.registerState.value
    val context = LocalContext.current
    val emailPattern = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$".toRegex()

    LaunchedEffect(state.data) {
        if (state.data != null) {
            Toast.makeText(context, "Đăng ký thành công! Vui lòng kiểm tra Email để xác thực.", Toast.LENGTH_LONG).show()
            viewModel.resetRegisterState()
            navController.popBackStack()
        }
    }

    AuthBackground {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("ĐĂNG KÝ TÀI KHOẢN", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = orange600)
            Spacer(modifier = Modifier.height(24.dp))

            AuthTextField(value = name, onValueChange = { name = it }, label = "Họ và tên")
            Spacer(modifier = Modifier.height(4.dp))

            AuthTextField(value = username, onValueChange = { username = it }, label = "Tên đăng nhập (Username)")
            Spacer(modifier = Modifier.height(4.dp))

            // 🔥 CHỈ CHO NHẬP SỐ, CHẶN KÝ TỰ KHÁC, TỐI ĐA 10 SỐ, BÁO LỖI NẾU CHƯA ĐỦ
            AuthTextField(
                value = phone,
                onValueChange = { input ->
                    val digitsOnly = input.filter { it.isDigit() }
                    if (digitsOnly.length <= 10) {
                        phone = digitsOnly
                        phoneError = if (digitsOnly.isNotEmpty() && digitsOnly.length < 10) "Số điện thoại phải đủ 10 số" else ""
                    }
                },
                label = "Số điện thoại",
                keyboardType = KeyboardType.Number,
                errorMessage = phoneError
            )
            Spacer(modifier = Modifier.height(4.dp))

            // 🔥 RÀNG BUỘC ĐỊNH DẠNG EMAIL
            AuthTextField(
                value = email,
                onValueChange = {
                    email = it
                    emailError = if (it.isNotEmpty() && !it.matches(emailPattern)) "Sai định dạng email (VD: abcd@xyz.yyy)" else ""
                },
                label = "Địa chỉ Email",
                errorMessage = emailError
            )
            Spacer(modifier = Modifier.height(4.dp))

            AuthTextField(value = password, onValueChange = { password = it }, label = "Mật khẩu", isPassword = true)
            Spacer(modifier = Modifier.height(4.dp))

            AuthTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = "Xác nhận lại mật khẩu",
                isPassword = true,
                errorMessage = if (confirmPassword.isNotEmpty() && confirmPassword != password) "Mật khẩu xác nhận không khớp" else ""
            )

            val displayError = localError.ifEmpty { state.errorMessage }
            if (displayError.isNotEmpty()) {
                Text(text = displayError, color = Color.Red, modifier = Modifier.padding(vertical = 12.dp), fontSize = 14.sp)
            } else {
                Spacer(modifier = Modifier.height(24.dp))
            }

            if (state.isLoading) {
                CircularProgressIndicator(color = orange600, modifier = Modifier.padding(16.dp))
            } else {
                AuthButton(
                    text = "ĐĂNG KÝ NGAY",
                    onClick = {
                        localError = ""
                        when {
                            email.isBlank() || password.isBlank() || confirmPassword.isBlank() || name.isBlank() || username.isBlank() || phone.isBlank() -> {
                                localError = "Vui lòng điền đầy đủ thông tin!"
                            }
                            phone.length < 10 -> {
                                localError = "Số điện thoại chưa hợp lệ!"
                            }
                            emailError.isNotEmpty() -> {
                                localError = "Vui lòng nhập đúng định dạng Email!"
                            }
                            password != confirmPassword -> {
                                localError = "Mật khẩu xác nhận không khớp!"
                            }
                            password.length < 6 -> {
                                localError = "Mật khẩu phải từ 6 ký tự trở lên."
                            }
                            else -> {
                                viewModel.register(email, password, name, phone, username)
                            }
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Đã có tài khoản?", color = gray800)
                TextButton(onClick = { navController.popBackStack() }, contentPadding = PaddingValues(horizontal = 4.dp)) {
                    Text("Đăng nhập ngay", color = orange600, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}