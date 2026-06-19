package com.example.handispace.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage // 🔥 Thêm thư viện Load ảnh

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    navController: NavController,
    viewModel: ChatListViewModel = hiltViewModel()
) {
    val orange600 = Color(0xFFEA580C)
    val rooms = viewModel.chatRooms.value
    val myId = viewModel.currentUserId

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hộp thoại tin nhắn", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Trở lại", tint = orange600)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF9FAFB)
    ) { padding ->
        if (viewModel.isLoading.value) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = orange600)
            }
        } else if (rooms.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Chưa có cuộc hội thoại nào.", color = Color.Gray, fontSize = 14.sp)
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
                items(rooms, key = { it.room_id }) { room ->

                    val otherUserId = room.participants.firstOrNull { it != myId } ?: "unknown"
                    val displayName = room.user_name
                    val unreadCount = if (myId == "axTeqn5PxRet3M5vYr1nvgbnp542") room.unread_by_admin else room.unread_by_user

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .clickable {
                                navController.navigate("chat/$otherUserId/${android.net.Uri.encode(displayName)}")
                            }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 🔥 LOGIC HIỂN THỊ AVATAR THẬT TỪ FIREBASE
                        Box(
                            modifier = Modifier.size(46.dp).clip(CircleShape).background(Color(0xFFE5E7EB)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (room.user_avatar.isNotBlank()) {
                                AsyncImage(
                                    model = room.user_avatar,
                                    contentDescription = "Avatar",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Icon(Icons.Filled.Person, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(26.dp))
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = displayName,
                                fontSize = 15.sp,
                                fontWeight = if (unreadCount > 0) FontWeight.Bold else FontWeight.Medium,
                                color = Color(0xFF1F2937)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = room.last_message,
                                fontSize = 13.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = if (unreadCount > 0) Color.Black else Color.Gray,
                                fontWeight = if (unreadCount > 0) FontWeight.Medium else FontWeight.Normal
                            )
                        }

                        // 🔥 HIỂN THỊ SỐ 10 (SỐ LƯỢNG TIN NHẮN TỪNG PHÒNG) Ở ĐÂY
                        if (unreadCount > 0) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(modifier = Modifier.size(20.dp).clip(CircleShape).background(Color.Red), contentAlignment = Alignment.Center) {
                                Text(unreadCount.toString(), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Divider(color = Color(0xFFF3F4F6), thickness = 1.dp)
                }
            }
        }
    }
}