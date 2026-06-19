package com.example.handispace.ui.user.notification

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(viewModel: NotificationViewModel) {
    LaunchedEffect(Unit) { viewModel.loadNotifications() }

    val state = viewModel.notificationsState.value
    val groupedNoti = state.data ?: emptyMap()
    val orange600 = Color(0xFFEA580C)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Thông báo mua sắm", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                actions = {
                    // 🔥 ĐÃ CHỈNH: Thay thế Icon hành động cũ bằng nút chữ TextButton gọn gàng, không icon
                    TextButton(onClick = { viewModel.markAllRead() }) {
                        Text("Đọc tất cả", color = orange600, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF9FAFB)
    ) { padding ->
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = orange600) }
        } else if (state.errorMessage != null && state.errorMessage.isNotEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                Text(state.errorMessage, color = Color.Red, fontSize = 14.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            }
        } else if (groupedNoti.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Không có thông báo nào.", color = Color.Gray, fontSize = 14.sp)
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
                groupedNoti.forEach { (date, notiList) ->
                    item {
                        Box(modifier = Modifier.fillMaxWidth().background(Color(0xFFF3F4F6)).padding(horizontal = 16.dp, vertical = 6.dp)) {
                            Text(text = date, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4B5563))
                        }
                    }


                    items(notiList, key = { it.id }) { noti ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(if (noti.is_read) Color.White else Color(0xFFFFF7ED))
                                .padding(16.dp)
                        ) {
                            Text(
                                text = noti.title,
                                fontSize = 14.sp,
                                fontWeight = if (noti.is_read) FontWeight.Medium else FontWeight.Bold,
                                color = if (noti.is_read) Color(0xFF4B5563) else Color(0xFF1F2937)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = noti.message,
                                fontSize = 13.sp,
                                fontWeight = if (noti.is_read) FontWeight.Normal else FontWeight.SemiBold,
                                color = if (noti.is_read) Color.Gray else Color(0xFF374151),
                                lineHeight = 18.sp
                            )
                        }
                        Divider(color = Color(0xFFF3F4F6), thickness = 1.dp)
                    }
                }
            }
        }
    }
}