package com.example.food2you.viewmodels

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.example.food2you.Repository
import com.example.food2you.data.local.entities.Restaurant
import com.example.food2you.other.Event
import com.example.food2you.other.Resource
import kotlinx.coroutines.launch

class RestaurantsViewModel
@ViewModelInject constructor(private val repository: Repository): ViewModel(){

    private val _forceUpdate = MutableLiveData<Boolean>(false)

    private val _allRestaurants = _forceUpdate.switchMap {
        repository.getAllRestaurants().asLiveData(viewModelScope.coroutineContext)
    }.switchMap {
        MutableLiveData(Event(it))
    }

    val allRestaurants = _allRestaurants


    var filteredRestaurants: LiveData<List<Restaurant>> = MutableLiveData()

    fun filter(type: String) {
        filteredRestaurants = repository.getRestaurantsByType(type)
    }

}