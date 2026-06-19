package com.example.handispace.model

import com.google.firebase.Timestamp

data class ChatRoom(
    val room_id: String = "",
    val participants: List<String> = emptyList(),
    val user_name: String = "",
    val user_avatar: String = "",
    val last_message: String = "",
    val last_timestamp: Timestamp? = null,
    val unread_by_admin: Int = 0,
    val unread_by_user: Int = 0
)

data class Message(
    val message_id: String = "",
    val sender_id: String = "",
    val text: String = "",
    val timestamp: Timestamp? = null
)