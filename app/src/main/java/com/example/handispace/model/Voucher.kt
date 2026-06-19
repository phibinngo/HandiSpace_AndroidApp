package com.example.handispace.model

import com.google.firebase.Timestamp

data class Voucher(
    val voucher_id: String = "",
    val code: String = "",
    val type: String = "",
    val discount_type: String = "",
    val discount_value: Double = 0.0,
    val min_order_value: Double = 0.0,
    val max_discount: Double = 0.0,
    val target_audience: String = "all",
    val usage_limit: Int = 0,
    val used_count: Int = 0,
    val applicable_categories: List<String> = emptyList(),
    val applicable_category_names: List<String> = emptyList(),
    val start_date: Timestamp? = null,
    val end_date: Timestamp? = null,
    val is_active: Boolean = true
)