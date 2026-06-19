package com.example.handispace.model

import com.google.firebase.Timestamp

data class Product(
    val product_id: String = "",
    val category_id: String = "",
    val category_name: String = "",
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val quantity: Int = 0,
    val sold_count: Int = 0,
    val images: List<String> = emptyList(),
    val rating_average: Double = 0.0,
    val review_count: Int = 0,
    val created_at: Timestamp? = null,
)