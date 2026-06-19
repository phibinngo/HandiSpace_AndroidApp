package com.example.handispace.model

import com.google.firebase.Timestamp

data class Cart(
    val user_id: String = "",
    val items: List<CartItem> = emptyList(),
    val updated_at: Timestamp? = null
)
data class CartItem(
    val product_id: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val quantity: Int = 0,
    val image: String = "",
    val max_stock: Int = 0,
    val category_id: String = ""
)