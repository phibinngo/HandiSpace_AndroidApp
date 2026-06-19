package com.example.handispace.model

import com.google.firebase.Timestamp

data class Order(
    val order_id: String = "",
    val user_id: String = "",
    val status: String = "pending",
    val payment_method: String = "COD",
    val subtotal: Double = 0.0,
    val shipping_fee: Double = 30000.0,
    val freeship_discount: Double = 0.0,
    val order_discount: Double = 0.0,
    val member_discount: Double = 0.0,
    val final_total: Double = 0.0,
    val shipping_info: ShippingInfo = ShippingInfo(),
    val note: String = "",
    val order_items: List<OrderItem> = emptyList(),
    val cancel_reason: String? = null,
    val return_reason: String? = null,
    val created_at: Timestamp? = null,
    val updated_at: Timestamp? = null
)

data class ShippingInfo(
    val name: String = "",
    val phone: String = "",
    val address: String = ""
)

data class OrderItem(
    val product_id: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val quantity: Int = 0,
    val image: String = "",
    val is_reviewed: Boolean = false
)