package com.example.food2you.viewmodels

import androidx.core.text.isDigitsOnly
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.food2you.other.Resource
import com.example.food2you.Repository
import com.example.food2you.data.remote.UserToken
import com.example.food2you.data.remote.models.Order
import kotlinx.coroutines.launch

class AuthViewModel @ViewModelInject constructor(private val repository: Repository): ViewModel() {

    private val _registerStatus = MutableLiveData<Resource<String>>()
    val registerStatus: LiveData<Resource<String>> = _registerStatus

    private val _loginStatus = MutableLiveData<Resource<String>>()
    val loginStatus: LiveData<Resource<String>> = _loginStatus




    fun register(email: String, password: String, confirmPassword: String, token: String) {
        _registerStatus.postValue(Resource.loading(null))

        if(email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            _registerStatus.postValue(Resource.error("All fields must be filled", null))
            return
        }

        if(password != confirmPassword) {
            _registerStatus.postValue(Resource.error("Passwords do not match", null))
            return
        }

        if(password.length < 6 || password.length > 18) {
            _registerStatus.postValue(Resource.error("Password must be between 6 and 18 characters", null))
            return
        }
        if(password.isDigitsOnly()) {
            _registerStatus.postValue(Resource.error("Password must contain at least one letter", null))
            return
        }
        if(!(password.matches(".*\\d+.*".toRegex()))) {
            _registerStatus.postValue(Resource.error("Password must contain at least one number", null))
            return
        }
        viewModelScope.launch {
            val result = repository.register(email, password, token)
            _registerStatus.postValue(result)
        }

    }

    fun login(email: String, password: String) {
        _loginStatus.postValue(Resource.loading(null))

        if(email.isEmpty() || password.isEmpty()) {
            _loginStatus.postValue(Resource.error("Please, fill out all the fields", null))
            return
        }
        viewModelScope.launch {
            val result = repository.login(email, password)
            _loginStatus.postValue(result)
        }
    }


    var waitingOrdersLiveData: MutableLiveData<List<Order>> = MutableLiveData(listOf())

    fun getAllWaitingOrders() = viewModelScope.launch {
        val list = repository.getAllWaitingOrdersForUser()
        waitingOrdersLiveData.postValue(list)
    }


    fun changeRecipientToken(token: String) = viewModelScope.launch {
        repository.changeRecipientToken(token)
    }

    fun registerUserToken(userToken: UserToken, email: String) = viewModelScope.launch {
        repository.registerUserToken(userToken, email)
    }

}