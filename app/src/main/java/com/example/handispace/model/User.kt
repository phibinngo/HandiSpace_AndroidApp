package com.example.handispace.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName // 🔥 Bắt buộc phải có import này

data class User(
    val uid: String = "",
    val name: String = "",
    val username: String = "",
    val email: String = "",
    val phone: String = "",
    val age: Int = 0,
    val role: String = "customer",
    val total_spent: Double = 0.0,
    val loyalty_level: LoyaltyLevel = LoyaltyLevel(),
    val avatar_url: String = "",
    val created_at: Timestamp? = null,
    val addresses: List<Address> = emptyList(),
    val saved_vouchers: List<String> = emptyList(),
    val used_vouchers: List<UsedVoucherInfo> = emptyList(),

    @get:PropertyName("is_disabled")
    @set:PropertyName("is_disabled")
    var is_disabled: Boolean = false
)

data class UsedVoucherInfo(
    val voucher_id: String = "",
    val used_at: Timestamp? = null
)

data class LoyaltyLevel(
    val level_name: String = "Bronze",
    val discount_percent: Double = 0.0
)

data class Address(
    val address_id: String = "",
    val address_name: String = "",
    val receiver_name: String = "",
    val receiver_phone: String = "",
    val province: String = "",
    val ward: String = "",
    val street_address: String = "",
    var is_default: Boolean = false
)