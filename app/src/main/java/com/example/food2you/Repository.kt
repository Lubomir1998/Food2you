package com.example.food2you

import android.app.Application
import com.example.food2you.data.local.ResDao
import com.example.food2you.data.local.entities.Food
import com.example.food2you.data.remote.models.Order
import com.example.food2you.data.local.entities.Restaurant
import com.example.food2you.data.remote.ApiService
import com.example.food2you.data.remote.requests.AccountRequest
import com.example.food2you.data.remote.requests.LikeRestaurantRequest
import com.example.food2you.other.Resource
import com.example.food2you.other.hasInternetConnection
import com.example.food2you.other.networkBoundResource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import retrofit2.Response
import javax.inject.Inject

class Repository
@Inject constructor(
    private val dao: ResDao,
    private val api: ApiService,
    private val context: Application
){

    suspend fun register(email: String, password: String) = withContext(Dispatchers.IO) {
        try{
            val response = api.register(AccountRequest(email, password))
            if(response.isSuccessful && response.body()!!.isSuccessful) {
                Resource.success(response.body()?.message)
            } else {
                Resource.error(response.body()?.message ?: response.message(), null)
            }
        } catch (e: Exception){
            Resource.error("Couldn't connect to servers. Check your internet connection", null)
        }
    }

    suspend fun login(email: String, password: String) = withContext(Dispatchers.IO) {
        try{
            val response = api.login(AccountRequest(email, password))
            if(response.isSuccessful && response.body()!!.isSuccessful) {
                Resource.success(response.body()?.message)
            } else {
                Resource.error(response.body()?.message ?: response.message(), null)
            }
        } catch (e: Exception){
            Resource.error("Couldn't connect to servers. Check your internet connection", null)
        }
    }

    suspend fun insertOrder(order: Order) = withContext(Dispatchers.IO) {
        try {
            val response = api.orderFood(order)
            if(response.isSuccessful && response.body()!!.isSuccessful) {
                Resource.success(response.body()?.message)
            }
            else {
                Resource.error(response.body()?.message ?: response.message(), null)
            }
        } catch (e: Exception){
            Resource.error("Couldn't connect to servers. Check your internet connection", null)
        }
    }

    suspend fun getRestaurantById(id: String) = dao.getRestaurantById(id)

    private suspend fun insertRestaurant(restaurant: Restaurant) = dao.insertRes(restaurant)

    private suspend fun insertFood(food: Food) = dao.insertFood(food)

    private var currentResponse: Response<List<Restaurant>>? = null

    private suspend fun sync() {
        currentResponse = api.getAllRestaurants()
        currentResponse?.body()?.let { restaurants ->
            dao.deleteAllRestaurants()
            restaurants.forEach { dao.insertRes(it) }
        }

    }


    fun getAllRestaurants(): Flow<Resource<List<Restaurant>>> {
        return networkBoundResource(
            query = {
                dao.getAllRestaurants()
            },
            fetch = {
                sync()
                currentResponse
            },
            savedFetchResult = {
                it?.body()?.let { restaurants ->
                    restaurants.forEach { dao.insertRes(it) }
                }
            },
            shouldFetch = {
                hasInternetConnection(context)
            }
        )
    }


    suspend fun likeRestaurant(restaurantId: String, user: String) = withContext(Dispatchers.IO) {
        val response = api.likeRestaurant(LikeRestaurantRequest(restaurantId, user))

        try {
            if(response.isSuccessful && response.body()!!.isSuccessful) {
                val restaurant = getRestaurantById(restaurantId)
                restaurant?.let {
                    it.users = it.users + user
                    insertRestaurant(it)
                }

                Resource.success(response.body()?.message)
            }
            else {
                Resource.error(response.body()?.message ?: response.message(), null)
            }

        } catch (e: Exception) {
            Resource.error("Couldn't connect to servers. Check your internet connection", null)
        }


    }

    suspend fun dislikeRestaurant(restaurantId: String, user: String) = withContext(Dispatchers.IO) {
        val response = api.dislikeRestaurant(LikeRestaurantRequest(restaurantId, user))

        try {
            if(response.isSuccessful && response.body()!!.isSuccessful) {
                val restaurant = getRestaurantById(restaurantId)
                restaurant?.let {
                    it.users = it.users - user
                    insertRestaurant(it)
                }

                Resource.success(response.body()?.message)
            }
            else {
                Resource.error(response.body()?.message ?: response.message(), null)
            }

        } catch (e: Exception) {
            Resource.error("Couldn't connect to servers. Check your internet connection", null)
        }


    }


    fun getRestaurantsByType(type: String) = dao.getRestaurantsByType(type)

    fun getFoodByType(type: String) = dao.getFoodByType(type)

    suspend fun getFavouriteRestaurants(): List<Restaurant>? {
        val result = try {
            api.getFavouriteRestaurants()
        } catch (e: Exception) {
            null
        }

        result?.let { response ->
            response.body()?.let { restaurants ->
                dao.deleteAllRestaurants()
                restaurants.forEach {
                    insertRestaurant(it)
                }
            }
            return dao._getAllRestaurants()
        }

        return null
    }

    suspend fun allRestaurants(): List<Restaurant>? {
        val result = try {
            api.getAllRestaurants()
        } catch (e: Exception) {
            null
        }

        result?.let { response ->
            response.body()?.let { restaurants ->
                dao.deleteAllRestaurants()
                restaurants.forEach {
                    insertRestaurant(it)
                }
            }
        }

        return dao._getAllRestaurants()
    }

    suspend fun getFoodForRestaurant(restaurant: String): List<Food>? {
        val result = try {
            api.getFoodForRestaurant(restaurant)
        } catch (e: Exception) {
            null
        }


        result?.let { response ->
            response.body()?.let {
                dao.deleteAllFood()
                it.forEach {
                    insertFood(it)
                }
            }
        }

        return dao.getFoodForRestaurant(restaurant)
    }


}