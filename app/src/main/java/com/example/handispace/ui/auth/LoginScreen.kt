package com.example.handispace.ui.auth

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.handispace.navigation.Routes
import com.example.handispace.ui.components.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: AuthViewModel
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf("") }

    val state = viewModel.loginState.value
    val context = LocalContext.current

    val emailPattern = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$".toRegex()

    val googleSignInClient = remember {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("224892602057-67b7kq34n9l573uc45443i3vfkjlrvko.apps.googleusercontent.com")
            .requestEmail()
            .build()
        GoogleSignIn.getClient(context, gso)
    }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            account?.idToken?.let { idToken ->
                viewModel.loginWithGoogle(idToken)
            }
        } catch (e: ApiException) {
            e.printStackTrace()
        }
    }

    LaunchedEffect(state.data) {
        if (state.data != null) {
            val userRole = state.data!!.role
            if (userRole == "admin") {
                navController.navigate(Routes.ADMIN_DASHBOARD) { popUpTo(Routes.LOGIN) { inclusive = true } }
            } else {
                navController.navigate(Routes.HOME) { popUpTo(Routes.LOGIN) { inclusive = true } }
            }
        }
    }

    if (!state.errorMessage.isNullOrEmpty()) {
        ErrorPopup(errorMessage = state.errorMessage!!, onDismiss = { viewModel.clearLoginError() })
    }

    AuthBackground {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("ĐĂNG NHẬP", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = orange600)
            Text("Chào mừng trở lại HandiSpace!", fontSize = 14.sp, color = gray500, modifier = Modifier.padding(top = 8.dp))
            Spacer(modifier = Modifier.height(32.dp))

            AuthTextField(
                value = email,
                onValueChange = {
                    email = it
                    emailError = if (it.isNotEmpty() && !it.matches(emailPattern)) "Sai định dạng email (VD: abcd@xyz.yyy)" else ""
                },
                label = "Email",
                errorMessage = emailError
            )
            Spacer(modifier = Modifier.height(4.dp))

            AuthTextField(value = password, onValueChange = { password = it }, label = "Mật khẩu", isPassword = true)

            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                TextButton(onClick = { navController.navigate(Routes.FORGOT_PASSWORD) }, contentPadding = PaddingValues(0.dp)) {
                    Text(text = "Quên mật khẩu?", color = orange600, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            if (state.isLoading) {
                CircularProgressIndicator(color = orange600, modifier = Modifier.padding(16.dp))
            } else {
                AuthButton(
                    text = "ĐĂNG NHẬP",
                    onClick = { viewModel.login(email, password) },
                    enabled = email.isNotBlank() && password.isNotBlank() && emailError.isEmpty()
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Divider(modifier = Modifier.weight(1f), color = Color.LightGray)
                Text(text = "HOẶC", fontWeight = FontWeight.Bold, color = gray500, modifier = Modifier.padding(horizontal = 16.dp))
                Divider(modifier = Modifier.weight(1f), color = Color.LightGray)
            }
            Spacer(modifier = Modifier.height(24.dp))

            @Suppress("DEPRECATION")
            AuthButton(
                text = "Đăng nhập nhanh bằng Google",
                onClick = {
                    googleSignInClient.signOut().addOnCompleteListener { launcher.launch(googleSignInClient.signInIntent) }
                },
                containerColor = Color(0xFFDB4437)
            )

            Spacer(modifier = Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Chưa có tài khoản?", color = gray800)
                TextButton(onClick = { navController.navigate(Routes.REGISTER) }, contentPadding = PaddingValues(horizontal = 4.dp)) {
                    Text("Đăng ký ngay", color = orange600, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}