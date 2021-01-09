package com.example.food2you.data.local.entities

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
@Entity
data class Restaurant(
        val name: String,
        val type: String,
        val kitchen: String,
        val deliveryPrice: Float,
        val deliveryTimeMinutes: Int,
        val minimalPrice: Float,
        val imgUrl: String,
        var previews: List<String>,
        var users: List<String>,
        val owner: String,
        @PrimaryKey(autoGenerate = false)
        val id: String = UUID.randomUUID().toString()
): Parcelable
