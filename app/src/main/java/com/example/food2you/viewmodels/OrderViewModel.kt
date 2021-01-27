package com.example.food2you.viewmodels

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.food2you.Repository
import com.example.food2you.data.remote.PushNotification
import com.example.food2you.data.remote.models.Order
import com.example.food2you.other.Resource
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

    fun sendPushNotification(pushNotification: PushNotification) = viewModelScope.launch {
        repository.sendPushNotification(pushNotification)
    }



}