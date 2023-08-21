package com.example.bobmukjaku

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class MapListViewModelFactory(private val repository: RestaurantRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MapListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MapListViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}