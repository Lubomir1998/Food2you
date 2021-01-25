package com.example.food2you.data.remote.models

import android.os.Parcelable
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
data class Order(
    val restaurant: String,
    val address: String,
    val recipient: String,
    val phoneNumber: Long,
    val food: List<FoodItem>,
    val price: Float,
    val timestamp: Long,
    val status: String,
    @PrimaryKey(autoGenerate = false)
    val id: String = UUID.randomUUID().toString()
): Parcelable
