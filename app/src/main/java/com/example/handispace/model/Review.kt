package com.example.handispace.model

import com.google.firebase.Timestamp

data class Review(
    val review_id: String = "",
    val product_id: String = "",
    val user_id: String = "",
    val user_name: String = "",
    val order_id: String = "",
    val rating: Double = 0.0,
    val content: String = "",
    val image_url: String = "",
    val created_at: Timestamp? = null
)