package com.example.food2you.viewmodels

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.food2you.Repository
import com.example.food2you.data.local.entities.Food
import com.example.food2you.data.local.entities.Restaurant
import com.example.food2you.other.Event
import com.example.food2you.other.Resource
import kotlinx.coroutines.launch

class DetailRestaurantViewModel
@ViewModelInject constructor(private val repository: Repository): ViewModel() {


    private val _restaurant = MutableLiveData<Event<Resource<Restaurant>>>()
    val restaurant: LiveData<Event<Resource<Restaurant>>> = _restaurant

    fun getRestaurantById(id: String) = viewModelScope.launch {
        _restaurant.postValue(Event(Resource.loading(null)))
        val res = repository.getRestaurantById(id)

        res?.let {
            _restaurant.postValue(Event(Resource.success(it)))
        } ?: _restaurant.postValue(Event(Resource.error("Restaurant not found", null)))
    }


    private var _foodList = MutableLiveData<Event<Resource<List<Food>>>>()
    val foodList: LiveData<Event<Resource<List<Food>>>> = _foodList


    fun getFood(restaurant: String) = viewModelScope.launch {
        _foodList.postValue(Event(Resource.loading(null)))

        val result = repository.getFoodForRestaurant(restaurant)

        result?.let {
            _foodList.postValue(Event(Resource.success(it)))
        } ?: _foodList.postValue(Event(Resource.error("Unknown error occurred", null)))
    }


    var filteredFood: LiveData<List<Food>> = MutableLiveData()

    fun filter(type: String) {
        filteredFood = repository.getFoodByType(type)
    }


}