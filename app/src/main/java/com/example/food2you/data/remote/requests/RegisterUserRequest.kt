package com.example.food2you.data.remote.requests

data class RegisterUserRequest(
    val email: String,
    val password: String,
    var token: String = ""
)
