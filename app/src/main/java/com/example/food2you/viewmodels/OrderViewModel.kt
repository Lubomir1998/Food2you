package com.example.food2you.viewmodels

import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.example.food2you.Repository
import com.example.food2you.data.remote.PushNotification
import com.example.food2you.data.remote.models.Order
import com.example.food2you.other.Event
import com.example.food2you.other.Resource
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class OrderViewModel
@ViewModelInject constructor(private val repository: Repository): ViewModel() {

    private val _orderStatus = MutableLiveData<Resource<String>>()
    val orderStatus: LiveData<Resource<String>> = _orderStatus

    fun order(order: Order) {
        _orderStatus.postValue(Resource.loading(null))

        viewModelScope.launch {
            val result = repository.insertOrder(order)
            _orderStatus.postValue(result)
        }
    }

    fun sendPushNotification(pushNotification: PushNotification) = GlobalScope.launch {
        repository.sendPushNotification(pushNotification)
    }



    private val _forceUpdate = MutableLiveData(false)

    private val _allOrders = _forceUpdate.switchMap {
        repository.getAllOrders().asLiveData(viewModelScope.coroutineContext)
    }.switchMap {
        MutableLiveData(Event(it))
    }

    val allOrders = _allOrders

    fun sync() = _forceUpdate.postValue(true)

}