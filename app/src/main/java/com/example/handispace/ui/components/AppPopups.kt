package com.example.handispace.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@Composable
fun SuccessPopup(message: String, onDismiss: () -> Unit) {
    var startAnimation by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { startAnimation = true }
    val iconScale by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "BouncingIcon"
    )

    Dialog(onDismissRequest = { onDismiss() }) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier.width(280.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 8.dp, bottom = 24.dp, start = 16.dp, end = 16.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    IconButton(onClick = { onDismiss() }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Filled.Close, contentDescription = "Đóng", tint = Color(0xFF6B7280))
                    }
                }
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = "Thành công",
                    tint = Color(0xFFF97316),
                    modifier = Modifier.size(64.dp).scale(iconScale)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(message, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            }
        }
    }
}

@Composable
fun ErrorPopup(errorMessage: String, onDismiss: () -> Unit) {
    var startAnimation by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { startAnimation = true }
    val iconScale by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "BouncingErrorIcon"
    )

    Dialog(onDismissRequest = { onDismiss() }) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier.width(280.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 8.dp, bottom = 24.dp, start = 16.dp, end = 16.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    IconButton(onClick = { onDismiss() }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Filled.Close, contentDescription = "Đóng", tint = Color(0xFF6B7280))
                    }
                }
                Icon(
                    imageVector = Icons.Filled.Cancel,
                    contentDescription = "Thất bại",
                    tint = Color(0xFFEF4444),
                    modifier = Modifier.size(64.dp).scale(iconScale)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(errorMessage, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1F2937), textAlign = androidx.compose.ui.text.style.TextAlign.Center, modifier = Modifier.padding(horizontal = 8.dp))
            }
        }
    }
}

@Composable
fun ConfirmDeletePopup(onConfirm: () -> Unit, onCancel: () -> Unit) {
    Dialog(onDismissRequest = { onCancel() }) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier.width(300.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Warning,
                    contentDescription = "Cảnh báo",
                    tint = Color(0xFFF59E0B),
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("Bạn có chắc chắn muốn bỏ sản phẩm này khỏi giỏ hàng?", fontSize = 15.sp, color = Color(0xFF1F2937), fontWeight = FontWeight.Medium, textAlign = androidx.compose.ui.text.style.TextAlign.Center)

                Spacer(modifier = Modifier.height(24.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    OutlinedButton(
                        onClick = { onCancel() },
                        modifier = Modifier.weight(1f).height(44.dp),
                        border = BorderStroke(1.dp, Color(0xFFD1D5DB)),
                        shape = RoundedCornerShape(8.dp)
                    ) { Text("Không", color = Color(0xFF6B7280)) }

                    Spacer(modifier = Modifier.width(12.dp))

                    Button(
                        onClick = { onConfirm() },
                        modifier = Modifier.weight(1f).height(44.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEA580C)),
                        shape = RoundedCornerShape(8.dp)
                    ) { Text("Đồng ý", color = Color.White) }
                }
            }
        }
    }
}

@Composable
fun ActionFormPopup(
    title: String,
    placeholder: String,
    confirmText: String,
    confirmColor: Color,
    onConfirm: (String) -> Unit,
    onCancel: () -> Unit
) {
    var inputText by remember { mutableStateOf("") }

    Dialog(onDismissRequest = { onCancel() }) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier.width(320.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(24.dp)
            ) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF1F2937))
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text(placeholder, fontSize = 14.sp, color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = confirmColor,
                        unfocusedBorderColor = Color(0xFFD1D5DB),
                        focusedContainerColor = Color(0xFFF9FAFB),
                        unfocusedContainerColor = Color(0xFFF9FAFB)
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = { onCancel() },
                        modifier = Modifier.weight(1f).height(44.dp),
                        border = BorderStroke(1.dp, Color(0xFFD1D5DB)),
                        shape = RoundedCornerShape(8.dp)
                    ) { Text("Hủy bỏ", color = Color(0xFF6B7280), fontWeight = FontWeight.Medium) }

                    Button(
                        onClick = { if (inputText.isNotBlank()) onConfirm(inputText) },
                        enabled = inputText.isNotBlank(),
                        modifier = Modifier.weight(1f).height(44.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = confirmColor),
                        shape = RoundedCornerShape(8.dp)
                    ) { Text(confirmText, color = Color.White, fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}