package com.example.food2you

import android.app.Application
import com.example.food2you.data.local.ResDao
import com.example.food2you.data.local.entities.Restaurant
import com.example.food2you.data.remote.ApiService
import com.example.food2you.other.Resource
import com.example.food2you.other.hasInternetConnection
import com.example.food2you.other.networkBoundResource
import kotlinx.coroutines.flow.Flow
import retrofit2.Response
import javax.inject.Inject

class Repository
@Inject constructor(
    private val dao: ResDao,
    private val api: ApiService,
    private val context: Application
){

    suspend fun getRestaurantById(id: String) = dao.getRestaurantById(id)


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




}