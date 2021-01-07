package com.example.food2you.data.remote.models

import java.util.*

data class Order(
    val restaurant: String,
    val address: String,
    val phoneNumber: Long,
    val food: List<FoodItem>,
    val price: Float,
    val timestamp: Long,
    val id: String = UUID.randomUUID().toString()
)
