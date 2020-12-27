package com.example.food2you.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class Restaurant(
    val name: String,
    val type: String,
    val kitchen: String,
    val deliveryPrice: Float,
    val deliveryTimeMinutes: Int,
    val minimalPrice: Float,
    val imgUrl: String,
    val previews: List<String>,
    val users: List<String>,
    val owner: String,
    @PrimaryKey(autoGenerate = false)
    val id: String = UUID.randomUUID().toString()
)
