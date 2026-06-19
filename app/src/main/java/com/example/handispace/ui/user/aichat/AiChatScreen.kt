package com.example.handispace.ui.user.aichat

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.handispace.navigation.Routes
import com.example.handispace.ui.components.ProductCard
import com.example.handispace.ui.components.SuccessPopup // Import Popup
import com.example.handispace.ui.components.ErrorPopup   // Import Popup
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiChatScreen(
    navController: NavController,
    viewModel: AiChatViewModel = hiltViewModel()
) {
    val chatHistory = viewModel.chatHistory.value
    val isLoading = viewModel.isLoading.value
    var inputText by remember { mutableStateOf("") }

    val listState = rememberLazyListState()

    // Biến trạng thái để hiển thị Popup
    var showSuccessPopup by remember { mutableStateOf(false) }
    var popupMessage by remember { mutableStateOf("") }

    val orange600 = Color(0xFFEA580C)
    val orange50 = Color(0xFFFFF7ED)
    val gray50 = Color(0xFFF9FAFB)

    // Thêm một biến để theo dõi số lượng tin nhắn cũ
    var previousChatSize by remember { mutableStateOf(0) }

    // XỬ LÝ CUỘN THÔNG MINH
    LaunchedEffect(chatHistory.size) {
        if (chatHistory.isNotEmpty()) {
            if (previousChatSize == 0) {
                // LẦN ĐẦU VÀO MÀN HÌNH: Nhảy THẲNG xuống cuối ngay lập tức, không chạy từ trên xuống
                listState.scrollToItem(chatHistory.size)
            } else if (chatHistory.size > previousChatSize) {
                // KHI CÓ TIN NHẮN MỚI (Đang chat): Cuộn mượt xuống dưới để người dùng dễ nhìn
                delay(1) // Một khoảng delay rất nhỏ để đảm bảo item mới đã render xong
                listState.animateScrollToItem(chatHistory.size)
            }
            // Cập nhật lại số lượng tin nhắn hiện tại
            previousChatSize = chatHistory.size
        }
    }

    // Hiển thị Popup
    if (showSuccessPopup) {
        SuccessPopup(
            message = popupMessage,
            onDismiss = { showSuccessPopup = false }
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize().imePadding(),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.AutoAwesome, contentDescription = "AI", tint = orange600)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Trợ lý AI HandiSpace", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Trở lại", tint = orange600)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        navController.navigate(Routes.CART)
                    }) {
                        Icon(Icons.Filled.ShoppingCart, contentDescription = "Giỏ hàng", tint = orange600)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier.fillMaxWidth().background(Color.White).padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text("Hỏi AI cách làm đồ handmade...") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = orange600,
                        unfocusedBorderColor = Color.LightGray
                    ),
                    maxLines = 3
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            viewModel.sendMessage(inputText)
                            inputText = ""
                        }
                    },
                    modifier = Modifier.size(48.dp).clip(RoundedCornerShape(24.dp)).background(if (inputText.isNotBlank() && !isLoading) orange600 else Color.LightGray),
                    enabled = inputText.isNotBlank() && !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Filled.Send, contentDescription = "Gửi", tint = Color.White)
                    }
                }
            }
        },
        containerColor = gray50
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { AiBubbleMessage("Chào bạn! Mình là trợ lý AI của HandiSpace. Bạn muốn mình gợi ý làm món đồ handmade nào hôm nay không?") }

            items(chatHistory) { message ->
                if (message.role == "user") {
                    UserBubbleMessage(message.text)
                } else {
                    val suggestRegex = "\\[SUGGEST:(.*?)\\]".toRegex()
                    val matchResult = suggestRegex.find(message.text)

                    // XỬ LÝ TEXT: Cắt thẻ Suggest và xóa Markdown
                    var textToShow = if (matchResult != null) message.text.replace(matchResult.value, "").trim() else message.text
                    textToShow = textToShow.replace("\\*\\*".toRegex(), "")
                    textToShow = textToShow.replace("\\* ".toRegex(), "- ") // Đổi bullet point

                    AiBubbleMessage(textToShow)

                    if (matchResult != null) {
                        val suggestedIds = matchResult.groups[1]?.value?.split(",")?.map { it.trim() } ?: emptyList()
                        val suggestedProducts = viewModel.allShopProducts.filter { it.product_id in suggestedIds }

                        if (suggestedProducts.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Column(
                                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(orange50).border(1.dp, orange600.copy(alpha = 0.3f), RoundedCornerShape(12.dp)).padding(12.dp)
                            ) {
                                Text("Bộ vật liệu gợi ý cho bạn:", fontWeight = FontWeight.Bold, color = orange600, fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(8.dp))

                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(suggestedProducts) { product ->
                                        Box(modifier = Modifier.width(140.dp)) {
                                            ProductCard(product = product, onClick = {
                                                navController.navigate(Routes.PRODUCT_DETAIL.replace("{productId}", product.product_id))
                                            })
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Button(
                                    onClick = {
                                        viewModel.addMultipleToCart(suggestedProducts) { isSuccess, msg ->
                                            if (isSuccess) {
                                                // Gọi Popup thay vì Toast
                                                popupMessage = msg
                                                showSuccessPopup = true
                                            }
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().height(40.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = orange600),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Filled.ShoppingCart, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Thêm trọn bộ vào Giỏ")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}



@Composable
fun UserBubbleMessage(text: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        Box(modifier = Modifier.fillMaxWidth(0.8f).clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 4.dp)).background(Color(0xFFEA580C)).padding(12.dp)) {
            Text(text, color = Color.White, fontSize = 15.sp, lineHeight = 22.sp)
        }
    }
}

@Composable
fun AiBubbleMessage(text: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
        Box(modifier = Modifier.fillMaxWidth(0.85f).clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 4.dp, bottomEnd = 16.dp)).background(Color.White).border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 4.dp, bottomEnd = 16.dp)).padding(12.dp)) {
            Text(text, color = Color(0xFF1F2937), fontSize = 15.sp, lineHeight = 24.sp)
        }
    }
}