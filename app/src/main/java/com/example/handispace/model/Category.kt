package com.example.handispace.model

import com.google.firebase.Timestamp

data class Category(
    val category_id: String = "",
    val name: String = "",
    val type: String = "",
    val created_at: Timestamp? = null
)