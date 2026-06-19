package com.example.handispace.ui.chat

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.handispace.data.repository.ChatRepository
import com.example.handispace.model.ChatRoom
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatListViewModel @Inject constructor(
    private val chatRepository: ChatRepository
) : ViewModel() {

    val chatRooms = mutableStateOf<List<ChatRoom>>(emptyList())
    val isLoading = mutableStateOf(true)
    val currentUserId = chatRepository.currentUserId

    val totalUnreadCount = mutableStateOf(0)

    init {
        listenToChatRooms()
    }

    private fun listenToChatRooms() {
        viewModelScope.launch {
            chatRepository.getMyChatRoomsRealtime().collectLatest { rooms ->
                chatRooms.value = rooms

                // 🔥 SỬA LOGIC: Chỉ đếm SỐ PHÒNG có tin chưa đọc (Thay vì cộng dồn số tin nhắn)
                totalUnreadCount.value = rooms.count { room ->
                    if (currentUserId == "axTeqn5PxRet3M5vYr1nvgbnp542") room.unread_by_admin > 0 else room.unread_by_user > 0
                }

                isLoading.value = false
            }
        }
    }
}