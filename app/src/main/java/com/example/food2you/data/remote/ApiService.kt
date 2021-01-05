package com.example.food2you.data.remote

import com.example.food2you.data.local.entities.Food
import com.example.food2you.data.remote.models.Order
import com.example.food2you.data.local.entities.Restaurant
import com.example.food2you.data.remote.requests.AccountRequest
import com.example.food2you.data.remote.requests.AddPreviewRequest
import com.example.food2you.data.remote.requests.LikeRestaurantRequest
import com.example.food2you.data.remote.resoponses.SimpleResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {

    @POST("/registerUser")
    suspend fun register(@Body request: AccountRequest): Response<SimpleResponse>

    @POST("/login")
    suspend fun login(@Body request: AccountRequest): Response<SimpleResponse>

    @GET("/getAllRestaurants")
    suspend fun getAllRestaurants(): Response<List<Restaurant>>

    @POST("/likeRestaurant")
    suspend fun likeRestaurant(@Body likeRestaurantRequest: LikeRestaurantRequest): Response<SimpleResponse>

    @POST("/dislikeRestaurant")
    suspend fun dislikeRestaurant(@Body likeRestaurantRequest: LikeRestaurantRequest): Response<SimpleResponse>

    @GET("/getFavouriteRestaurants")
    suspend fun getFavouriteRestaurants(): Response<List<Restaurant>>

    @GET("/getFood/{restaurant}")
    suspend fun getFoodForRestaurant(@Path("restaurant") restaurant: String): Response<List<Food>>

    @POST("/addPreview")
    suspend fun addReview(@Body addPreviewRequest: AddPreviewRequest): Response<SimpleResponse>

    @POST("/order")
    suspend fun orderFood(@Body order: Order): Response<SimpleResponse>

}