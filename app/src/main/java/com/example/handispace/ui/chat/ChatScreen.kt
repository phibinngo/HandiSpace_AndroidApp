package com.example.handispace.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    navController: NavController,
    viewModel: ChatViewModel,
    targetUserId: String,
    targetUserName: String,
    initialContext: String? = null
) {
    LaunchedEffect(targetUserId) {
        viewModel.initChat(targetUserId)
    }

    var messageText by remember { mutableStateOf(TextFieldValue(initialContext ?: "")) }

    val messages = viewModel.messages.value
    val myId = viewModel.myUserId
    val orange600 = Color(0xFFEA580C)

    val decodedName = remember(targetUserName) { android.net.Uri.decode(targetUserName) }

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.scrollToItem(0)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(decodedName, fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Trở lại", tint = orange600)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(8.dp)
                    .imePadding(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    placeholder = { Text("Nhập tin nhắn...") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        autoCorrect = false
                    ),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = orange600,
                        unfocusedBorderColor = Color.LightGray
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        if (messageText.text.isNotBlank()) {
                            viewModel.sendMessage(messageText.text)
                            messageText = TextFieldValue("")
                            coroutineScope.launch {
                                if (messages.isNotEmpty()) listState.scrollToItem(0)
                            }
                        }
                    },
                    modifier = Modifier.background(orange600, RoundedCornerShape(50)).size(48.dp)
                ) {
                    Icon(Icons.Filled.Send, contentDescription = "Gửi", tint = Color.White)
                }
            }
        },
        containerColor = Color(0xFFF9FAFB)
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            reverseLayout = true
        ) {
            items(
                items = messages.reversed(),
                key = { it.message_id.ifEmpty { it.timestamp.hashCode() } }
            ) { msg ->
                val isMe = msg.sender_id == myId
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                color = if (isMe) orange600 else Color(0xFFE5E7EB),
                                shape = RoundedCornerShape(
                                    topStart = 16.dp,
                                    topEnd = 16.dp,
                                    bottomStart = if (isMe) 16.dp else 4.dp,
                                    bottomEnd = if (isMe) 4.dp else 16.dp
                                )
                            )
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                            .widthIn(max = 280.dp)
                    ) {
                        Text(
                            text = msg.text,
                            color = if (isMe) Color.White else Color.Black,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }
    }
}
