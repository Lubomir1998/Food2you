package com.example.food2you.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class Food(
    val name: String,
    val description: String,
    val type: String,
    val weight: Int,
    val imgUrl: String,
    val price: Float,
    val restaurantName: String,
    @PrimaryKey(autoGenerate = false)
    val id: String = UUID.randomUUID().toString()
)
