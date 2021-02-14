package com.example.food2you.data.remote.models

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.food2you.data.remote.models.FoodItem
import com.google.gson.annotations.Expose
import kotlinx.android.parcel.Parcelize
import java.util.*

@Entity
@Parcelize
data class Order(
    val restaurant: String,
    val address: String,
    val recipient: String,
    val email: String,
    val phoneNumber: String,
    val food: List<FoodItem>,
    val price: Float,
    val timestamp: Long,
    val status: String,
    val resImgUrl: String,
    val restaurantName: String,
    var resId: String = "",
    @PrimaryKey(autoGenerate = false)
    val id: String = UUID.randomUUID().toString()
): Parcelable
