package com.example.food2you

import android.app.Application
import com.example.food2you.data.local.ResDao
import com.example.food2you.data.remote.ApiService
import javax.inject.Inject

class Repository
@Inject constructor(
    private val dao: ResDao,
    private val api: ApiService,
    private val context: Application
){



}