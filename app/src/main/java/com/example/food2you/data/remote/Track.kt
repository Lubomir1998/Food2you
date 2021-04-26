package com.example.food2you.data.remote

import java.util.*

data class Track(
    val orderId: String,
    val coordinates: List<Coordinates>,
    val id: String = UUID.randomUUID().toString()
)
