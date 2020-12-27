package com.example.food2you.other

import okhttp3.Credentials
import okhttp3.Interceptor
import okhttp3.Response

class BasicAuthInterceptor: Interceptor {

    var email: String? = null
    var password: String? = null

    val ignoreAuthPaths = listOf("/registerRestaurant", "/login")

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        if(request.url.encodedPath in ignoreAuthPaths){
            return chain.proceed(request)
        }
        val authenticatedRequest = request.newBuilder()
            .header("Authorization", Credentials.basic(email ?: "", password ?: ""))
            .build()

        return chain.proceed(authenticatedRequest)
    }

}