package com.example.food2you.data.local

import androidx.room.TypeConverter
import com.example.food2you.data.remote.models.FoodItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

class Converters {

    @TypeConverter
    fun fromList(list: List<String>): String {
        return Gson().toJson(list)
    }

    @TypeConverter
    fun toList(string: String): List<String> {
        return Gson().fromJson(string, object : TypeToken<List<String>>() {}.type)
    }

    @TypeConverter
    fun fromCountryLangList(foodItem: List<FoodItem?>?): String? {
        if (foodItem == null) {
            return null
        }
        val gson = Gson()
        val type: Type = object : TypeToken<List<FoodItem?>?>() {}.type
        return gson.toJson(foodItem, type)
    }

    @TypeConverter
    fun toCountryLangList(foodItem: String?): List<FoodItem>? {
        if (foodItem == null) {
            return null
        }
        val gson = Gson()
        val type: Type = object : TypeToken<List<FoodItem?>?>() {}.type
        return gson.fromJson<List<FoodItem>>(foodItem, type)
    }

}