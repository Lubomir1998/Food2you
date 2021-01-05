package com.example.food2you.data.remote.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class FoodItems: ArrayList<FoodItem>(), Parcelable {
}