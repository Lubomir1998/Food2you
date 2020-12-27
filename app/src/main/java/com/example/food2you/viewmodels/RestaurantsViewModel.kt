package com.example.food2you.viewmodels

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import com.example.food2you.Repository

class RestaurantsViewModel
@ViewModelInject constructor(private val repository: Repository): ViewModel(){


}