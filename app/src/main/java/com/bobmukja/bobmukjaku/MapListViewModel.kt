package com.bobmukja.bobmukjaku

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bobmukja.bobmukjaku.Model.RestaurantList

class MapListViewModel(private val repository: RestaurantRepository) : ViewModel() {
    // LiveData를 사용하여 음식점 리스트를 관리
    private val _restaurantList = MutableLiveData<List<RestaurantList>>()
    val restaurantList: LiveData<List<RestaurantList>> get() = _restaurantList

    // UI에서 호출하는 함수로 음식점 정보를 가져옴
    suspend fun fetchRestaurantList(categoryList: String) {
        _restaurantList.value = repository.getRestaurantList(categoryList)
    }
}