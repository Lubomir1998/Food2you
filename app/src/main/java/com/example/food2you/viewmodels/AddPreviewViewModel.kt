package com.example.food2you.viewmodels

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.food2you.Repository
import com.example.food2you.other.Event
import com.example.food2you.other.Resource
import kotlinx.coroutines.launch

class AddPreviewViewModel
@ViewModelInject constructor(private val repository: Repository): ViewModel(){

    private val _addPreviewStatus = MutableLiveData<Event<Resource<String>>>()
    val addPreviewStatus: LiveData<Event<Resource<String>>> = _addPreviewStatus

    fun addPreview(id: String, preview: String) = viewModelScope.launch {
        val result = repository.addPreview(id, preview)

        _addPreviewStatus.postValue(Event(result))
    }


}