package com.example.handispace.model

import com.google.firebase.Timestamp

data class Notification(
    val id: String = "",
    val user_id: String = "",
    val title: String = "",
    val message: String = "",
    val type: String = "order",
    val is_read: Boolean = false,
    val created_at: Timestamp? = null
)