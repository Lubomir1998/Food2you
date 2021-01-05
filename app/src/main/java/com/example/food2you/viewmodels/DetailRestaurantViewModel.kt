package com.example.food2you.viewmodels

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.example.food2you.Repository
import com.example.food2you.data.local.entities.Food
import com.example.food2you.data.local.entities.Restaurant
import com.example.food2you.data.remote.models.FoodItem
import com.example.food2you.other.Event
import com.example.food2you.other.Resource
import kotlinx.coroutines.launch

class DetailRestaurantViewModel
@ViewModelInject constructor(private val repository: Repository): ViewModel() {


    var orderList = MutableLiveData<ArrayList<FoodItem>>(arrayListOf())
    var _orderList = ArrayList<FoodItem>()

    var orderPrice = MutableLiveData<Float>(0f)

    fun increasePrice(price: Float) {
        orderPrice.postValue(orderPrice.value?.plus(price))
    }

    private var list = arrayListOf<String>()
//    var quantity = 1

    fun addToList(item: FoodItem) {
        orderList.postValue(orderList.value?.let {
//
//            for(food in orderList.value!!) {
//                list.add(food.name)
//            }
//
//            if(list.contains(item.name)) {
//                quantity += 1
//                it.remove(item)
//                it.add(FoodItem(item.name, item.price * quantity, quantity))
//                _orderList.add(FoodItem(item.name, item.price * quantity, quantity))
//                it
//            }
//            else {
//                quantity = 1
                it.add(item)
//                _orderList.add(item)
                it
//            }


        })
    }


    private val _forceUpdate = MutableLiveData<Boolean>(false)

    private val _allRestaurants = _forceUpdate.switchMap {
        repository.getAllRestaurants().asLiveData(viewModelScope.coroutineContext)
    }.switchMap {
        MutableLiveData(Event(it))
    }

    val allRestaurants = _allRestaurants


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


    private val _likeStatus = MutableLiveData<Event<Resource<String>>>()
    val likeStatus: LiveData<Event<Resource<String>>> = _likeStatus

    private val _dislikeStatus = MutableLiveData<Event<Resource<String>>>()
    val dislikeStatus: LiveData<Event<Resource<String>>> = _dislikeStatus


    fun likeRestaurant(restaurantId: String, email: String) = viewModelScope.launch {
        val result = repository.likeRestaurant(restaurantId, email)

        _likeStatus.postValue(Event(result))
    }

    fun dislikeRestaurant(restaurantId: String, email: String) = viewModelScope.launch {
        val result = repository.dislikeRestaurant(restaurantId, email)

        _dislikeStatus.postValue(Event(result))
    }


}