package com.example.food2you.data.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.food2you.data.local.entities.Food
import com.example.food2you.data.local.entities.Restaurant
import kotlinx.coroutines.flow.Flow

@Dao
interface ResDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRes(restaurant: Restaurant)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFood(food: Food)

    @Query("SELECT * FROM restaurant")
    fun getAllRestaurants(): Flow<List<Restaurant>>

    @Query("SELECT * FROM food WHERE restaurantName = :restaurant")
    fun getFoodForRestaurant(restaurant: String): LiveData<List<Food>>

    @Query("SELECT * FROM restaurant WHERE type = :type")
    fun getRestaurantsByType(type: String): LiveData<List<Restaurant>>

    @Query("DELETE FROM restaurant")
    suspend fun deleteAllRestaurants()

    @Query("DELETE FROM food")
    suspend fun deleteAllFood()

    @Query("SELECT * FROM restaurant WHERE id = :id")
    suspend fun getRestaurantById(id: String): Restaurant?

}