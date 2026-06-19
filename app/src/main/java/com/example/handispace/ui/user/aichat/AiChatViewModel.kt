package com.example.handispace.ui.user.aichat

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.handispace.data.repository.user.CartRepository
import com.example.handispace.data.repository.user.ProductRepository
import com.example.handispace.model.AiMessage
import com.example.handispace.model.CartItem
import com.example.handispace.model.Product
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class AiChatViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val cartRepository: CartRepository
) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val currentUserId = auth.currentUser?.uid

    private val apiKey = ""

    val chatHistory = mutableStateOf<List<AiMessage>>(emptyList())
    val isLoading = mutableStateOf(false)
    var allShopProducts = listOf<Product>()

    init {
        loadChatHistory()
        loadProductsForAI()
    }

    private fun loadChatHistory() {
        if (currentUserId == null) return
        viewModelScope.launch {
            try {
                val doc = db.collection("ai_chat_history").document(currentUserId).get().await()
                if (doc.exists()) {
                    val messages = doc.get("messages") as? List<Map<String, String>> ?: emptyList()
                    chatHistory.value = messages.map {
                        AiMessage(role = it["role"] ?: "user", text = it["text"] ?: "")
                    }
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    private fun loadProductsForAI() {
        viewModelScope.launch {
            val result = productRepository.getProducts("all")
            allShopProducts = result.data ?: emptyList()
        }
    }

    fun sendMessage(userText: String) {
        if (userText.isBlank() || currentUserId == null) return

        viewModelScope.launch {
            isLoading.value = true
            val newUserMsg = AiMessage("user", userText)

            // Cập nhật UI tin nhắn của User
            chatHistory.value = chatHistory.value + newUserMsg

            try {
                val productContext = allShopProducts.joinToString("\n") {
                    "- Tên: ${it.name}, ID: ${it.product_id}, Giá: ${it.price}, Kho: ${it.quantity}"
                }

                val systemPrompt = """
    Bạn là trợ lý AI chuyên nghiệp, nhiệt tình của shop đồ handmade HandiSpace. Bạn thân thiện, xưng "Mình" và gọi khách là "Bạn".
    
    Nhiệm vụ của bạn:
    1. Hướng dẫn cách làm đồ handmade.
    2. Trợ lý tìm kiếm nhanh: Tra cứu hàng trong kho, báo giá và tư vấn cho khách.
    3. Chốt sale: Gợi ý các vật liệu có sẵn để khách mua luôn.
    
    Đây là danh sách hàng hóa ĐANG CÓ SẴN trong kho của shop:
    $productContext
    
    QUY TẮC BẮT BUỘC: 
    1. XỬ LÝ KHI KHÔNG CÓ HÀNG: Nếu khách tìm một món không có trong danh sách trên, BẮT BUỘC phải trả lời: "Rất tiếc, hiện tại món này bên mình tạm hết hoặc chưa bán." và chủ động gợi ý món khác tương tự (nếu có). Tuyệt đối không tự bịa ra sản phẩm.
    2. ẨN MÃ SẢN PHẨM (ID): TUYỆT ĐỐI KHÔNG hiển thị mã ID của sản phẩm trong nội dung chat gửi cho khách. Bạn chỉ được nhắc đến tên sản phẩm, công dụng và giá tiền để câu trả lời tự nhiên.
    3. CÚ PHÁP HIỂN THỊ UI: Khi bạn tra cứu thành công hoặc gợi ý mua hàng, bạn PHẢI gom tất cả ID của các sản phẩm đó và chèn vào một đoạn mã ẩn ở DÒNG CUỐI CÙNG của tin nhắn theo định dạng: [SUGGEST: ID1, ID2].
    
    Ví dụ cách trả lời ĐÚNG CHUẨN:
    "Mình tìm thấy một số mẫu hoa khô rất đẹp và hợp với bạn nè:
    - Hoa khô trang trí nến: Rất hợp để làm nến thơm handmade. Giá chỉ 25,000đ.
    - Lọ thủy tinh đựng hoa khô mix: Điểm nhấn tuyệt vời cho góc học tập. Giá 40,000đ.
    [SUGGEST: prod_cat_10_7, prod_cat_20_3]"
""".trimIndent()

                val generativeModel = GenerativeModel(
                    modelName = "gemini-3.1-flash-lite",
                    apiKey = apiKey,
                    systemInstruction = content { text(systemPrompt) }
                )

                // Truyền lịch sử bỏ đi tin nhắn cuối (vì nó là tin nhắn user vừa gửi, sẽ được truyền qua sendMessage)
                val chat = generativeModel.startChat(
                    history = chatHistory.value.dropLast(1).map {
                        content(role = it.role) { text(it.text) }
                    }
                )

                val response = chat.sendMessage(userText)
                val aiReplyText = response.text ?: "Xin lỗi, hệ thống AI đang bận."

                val newAiMsg = AiMessage("model", aiReplyText)

                // CHÚ Ý CHỖ NÀY: Dùng toán tử "+" để tạo một List mới hoàn toàn, ép Compose cập nhật UI
                chatHistory.value = chatHistory.value + newAiMsg

                // Lưu vào Firestore
                val historyToSave = chatHistory.value.map { mapOf("role" to it.role, "text" to it.text) }
                db.collection("ai_chat_history").document(currentUserId).set(mapOf("messages" to historyToSave))

            } catch (e: Exception) {
                // Phân tích lỗi để đưa ra câu trả lời thân thiện với người dùng
                val errorMessage = e.message ?: ""
                val friendlyReply = if (errorMessage.contains("503") || errorMessage.contains("high demand") || errorMessage.contains("UNAVAILABLE")) {
                    "Xin lỗi bạn, hệ thống AI hiện đang có quá nhiều người truy cập nên hơi nghẽn một chút. Bạn chờ vài phút rồi thử lại giúp mình nhé!"
                } else if (errorMessage.contains("serialization", ignoreCase = true)) {
                    "Hệ thống đang bảo trì máy chủ tạm thời, bạn vui lòng thử lại sau ít phút nha."
                } else {
                    "Xin lỗi, kết nối của mình đang bị gián đoạn. Bạn vui lòng gửi lại tin nhắn nhé!"
                }

                val errorMsg = AiMessage("model", friendlyReply)
                chatHistory.value = chatHistory.value + errorMsg
            } finally {
                isLoading.value = false
            }
        }
    }

    fun addMultipleToCart(productsToBuy: List<Product>, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                for (product in productsToBuy) {
                    if (product.quantity > 0) {
                        val cartItem = CartItem(
                            product_id = product.product_id,
                            name = product.name,
                            price = product.price,
                            quantity = 1,
                            image = if (product.images.isNotEmpty()) product.images[0] else "",
                            max_stock = product.quantity
                        )
                        cartRepository.addToCart(cartItem)
                    }
                }
                onResult(true, "Đã thêm trọn bộ vật liệu vào giỏ hàng!")
            } catch (e: Exception) {
                onResult(false, "Lỗi khi thêm vào giỏ hàng.")
            }
        }
    }
}