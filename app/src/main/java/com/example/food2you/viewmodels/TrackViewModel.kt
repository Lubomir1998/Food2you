package com.example.food2you.viewmodels

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.food2you.Repository
import com.example.food2you.data.remote.Track
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TrackViewModel
@ViewModelInject constructor(private val repository: Repository): ViewModel() {

    private val _coordinates = MutableStateFlow<Track?>(null)

    val coordinates: StateFlow<Track?> = _coordinates


    fun getCoordinates(orderId: String) {
        viewModelScope.launch {
            val flowTrack = repository.getTrack(orderId)

            flowTrack.collect {
                _coordinates.value = it
            }

        }
    }

}