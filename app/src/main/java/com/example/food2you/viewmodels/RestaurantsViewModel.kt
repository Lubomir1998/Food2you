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

    private val _forceUpdate = MutableLiveData(false)

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


    private val _favouriteRestaurants: MutableLiveData<Event<Resource<List<Restaurant>>>> = MutableLiveData()

    val favouriteRestaurants: LiveData<Event<Resource<List<Restaurant>>>> = _favouriteRestaurants

    fun getFavouriteRestaurants() {
        viewModelScope.launch {
            _favouriteRestaurants.postValue(Event(Resource.loading(null)))

            val result = repository.getFavouriteRestaurants()

            result?.let {
                _favouriteRestaurants.postValue(Event((Resource.success(it))))
            } ?: _favouriteRestaurants.postValue(Event(Resource.error("Unknown error occurred", null)))

        }
    }

    fun sync() = _forceUpdate.postValue(true)


}