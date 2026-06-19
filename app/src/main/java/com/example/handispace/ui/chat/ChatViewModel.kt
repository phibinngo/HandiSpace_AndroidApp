package com.example.handispace.ui.chat

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.handispace.data.repository.ChatRepository
import com.example.handispace.model.Message
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    val messages = mutableStateOf<List<Message>>(emptyList())
    var currentRoomId = mutableStateOf<String?>(null)
    val myUserId = auth.currentUser?.uid ?: ""

    fun initChat(otherUserId: String) {
        viewModelScope.launch {
            val roomResult = chatRepository.createOrGetRoom(otherUserId)

            roomResult.data?.let { roomId ->
                currentRoomId.value = roomId
                // 🔥 Đánh dấu đã đọc ngay khi vừa vào phòng
                chatRepository.markRoomAsRead(roomId)
                listenForMessages(roomId)
            }
        }
    }

    private fun listenForMessages(roomId: String) {
        viewModelScope.launch {
            chatRepository.getMessagesRealtime(roomId).collectLatest { msgs ->
                messages.value = msgs
                // 🔥 Đảm bảo tin nhắn tới lúc đang mở khung chat cũng được đánh dấu đã đọc
                chatRepository.markRoomAsRead(roomId)
            }
        }
    }

    fun sendMessage(text: String) {
        val roomId = currentRoomId.value ?: return
        if (text.isBlank()) return

        viewModelScope.launch {
            chatRepository.sendMessage(roomId, text)
        }
    }
}