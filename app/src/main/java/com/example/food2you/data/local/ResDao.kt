package com.example.food2you.data.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.food2you.data.local.entities.Food
import com.example.food2you.data.remote.models.Order
import com.example.food2you.data.local.entities.Restaurant
import kotlinx.coroutines.flow.Flow

@Dao
interface ResDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRes(restaurant: Restaurant)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFood(food: Food)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: Order)

    @Query("SELECT * FROM `order` ORDER BY timestamp DESC")
    fun getAllOrdersForUser(): Flow<List<Order>>

    @Query("SELECT * FROM restaurant")
    fun getAllRestaurants(): Flow<List<Restaurant>>

    @Query("SELECT * FROM food WHERE restaurantName = :restaurant")
    suspend fun getFoodForRestaurant(restaurant: String): List<Food>?

    @Query("SELECT * FROM restaurant WHERE type = :type")
    fun getRestaurantsByType(type: String): LiveData<List<Restaurant>>

    @Query("SELECT * FROM food WHERE type = :type")
    fun getFoodByType(type: String): LiveData<List<Food>>

    @Query("DELETE FROM restaurant")
    suspend fun deleteAllRestaurants()

    @Query("DELETE FROM food")
    suspend fun deleteAllFood()

    @Query("DELETE FROM `order`")
    suspend fun deleteAllOrders()

    @Query("SELECT * FROM restaurant WHERE id = :id")
    suspend fun getRestaurantById(id: String): Restaurant?

}