package com.example.food2you.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.food2you.data.local.entities.Food
import com.example.food2you.data.local.entities.Restaurant

@Dao
interface ResDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRes(restaurant: Restaurant)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFood(food: Food)

    @Query("DELETE FROM restaurant")
    suspend fun deleteAllRestaurants()

    @Query("DELETE FROM food")
    suspend fun deleteAllFood()

}