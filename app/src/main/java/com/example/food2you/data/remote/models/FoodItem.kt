package com.example.food2you.data.remote.models

import android.os.Parcel
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class FoodItem(
    val name: String,
    val price: Float,
    var quantity: Int = 1
): Parcelable
