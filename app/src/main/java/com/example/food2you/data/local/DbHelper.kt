package com.example.food2you.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.food2you.data.local.entities.Food
import com.example.food2you.data.remote.models.Order
import com.example.food2you.data.local.entities.Restaurant

@Database(entities = [Restaurant::class, Food::class, Order::class], version = 10)
@TypeConverters(Converters::class)
abstract class DbHelper: RoomDatabase() {

    abstract fun getDao(): ResDao

}