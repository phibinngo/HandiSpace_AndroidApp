package com.example.handispace.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val orange600 = Color(0xFFEA580C)
val orange50 = Color(0xFFFFF7ED)
val gray50 = Color(0xFFF9FAFB)
val gray500 = Color(0xFF6B7280)
val gray800 = Color(0xFF1F2937)

@Composable
fun AuthBackground(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gray50)
    ) {
        // Vẽ họa tiết nền bằng Code (Không cần hình ảnh ngoài)
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = orange50.copy(alpha = 0.8f),
                radius = size.width * 0.6f,
                center = Offset(0f, 0f)
            )
            drawCircle(
                color = orange600.copy(alpha = 0.1f),
                radius = size.width * 0.4f,
                center = Offset(size.width, size.height * 0.2f)
            )
            drawCircle(
                color = orange50.copy(alpha = 0.5f),
                radius = size.width * 0.5f,
                center = Offset(size.width * 0.2f, size.height)
            )
        }
        content()
    }
}

@Composable
fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    errorMessage: String = "" // Bổ sung bắt lỗi tại ô
) {
    var passwordVisible by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = gray500) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = if (isPassword) KeyboardType.Password else keyboardType),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = orange600,
            unfocusedBorderColor = Color(0xFFD1D5DB),
            focusedLabelColor = orange600,
            errorBorderColor = Color.Red,
            errorLabelColor = Color.Red,
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White
        ),
        isError = errorMessage.isNotEmpty(),
        supportingText = {
            if (errorMessage.isNotEmpty()) {
                Text(text = errorMessage, color = Color.Red, fontSize = 12.sp)
            }
        },
        trailingIcon = {
            if (isPassword) {
                val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                val description = if (passwordVisible) "Ẩn mật khẩu" else "Hiện mật khẩu"

                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = image, contentDescription = description, tint = gray500)
                }
            }
        }
    )
}

@Composable
fun AuthButton(
    text: String,
    onClick: () -> Unit,
    containerColor: Color = orange600, // Đổi màu mặc định sang màu Cam Chủ Đạo
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            disabledContainerColor = containerColor.copy(alpha = 0.5f)
        )
    ) {
        Text(text = text, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}