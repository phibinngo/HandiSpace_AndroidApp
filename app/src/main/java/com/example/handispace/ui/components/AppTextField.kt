package com.example.handispace.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp

// 🔥 BẢN 1: Dùng cho các màn hình bình thường (Thêm/Sửa sản phẩm, Địa chỉ...)
@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String? = null,
    placeholder: String = "",
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    modifier: Modifier = Modifier.fillMaxWidth()
) {
    var textFieldValue by remember { mutableStateOf(TextFieldValue(text = value)) }

    // 🔥 CÁCH FIX CỦA GOOGLE: Bỏ LaunchedEffect (bất đồng bộ).
    // Kiểm tra và đồng bộ ngay lập tức để không làm vỡ bộ gõ tiếng Việt.
    if (value != textFieldValue.text) {
        textFieldValue = TextFieldValue(
            text = value,
            selection = TextRange(value.length)
        )
    }

    OutlinedTextField(
        value = textFieldValue,
        onValueChange = { newValue ->
            // Ưu tiên cập nhật state nội bộ trước để giữ nguyên con trỏ
            textFieldValue = newValue
            if (newValue.text != value) {
                onValueChange(newValue.text)
            }
        },
        label = if (label != null) { { Text(label, color = Color(0xFF6B7280)) } } else null,
        placeholder = { Text(placeholder, color = Color(0xFF9CA3AF)) },
        keyboardOptions = keyboardOptions,
        modifier = modifier,
        singleLine = true,
        shape = RoundedCornerShape(8.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFFEA580C),
            unfocusedBorderColor = Color(0xFFD1D5DB),
            cursorColor = Color(0xFFEA580C),
            focusedTextColor = Color(0xFF1F2937),
            unfocusedTextColor = Color(0xFF1F2937)
        )
    )
}

// 🔥 BẢN 2 (MỚI): HÀM OVERLOAD CHUYÊN TRỊ CHO MÀN HÌNH CHAT (Giữ con trỏ hoàn hảo 100%)
@Composable
fun AppTextField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    label: String? = null,
    placeholder: String = "",
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    modifier: Modifier = Modifier.fillMaxWidth()
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = if (label != null) { { Text(label, color = Color(0xFF6B7280)) } } else null,
        placeholder = { Text(placeholder, color = Color(0xFF9CA3AF)) },
        keyboardOptions = keyboardOptions,
        modifier = modifier,
        singleLine = true,
        shape = RoundedCornerShape(8.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFFEA580C),
            unfocusedBorderColor = Color(0xFFD1D5DB),
            cursorColor = Color(0xFFEA580C),
            focusedTextColor = Color(0xFF1F2937),
            unfocusedTextColor = Color(0xFF1F2937)
        )
    )
}