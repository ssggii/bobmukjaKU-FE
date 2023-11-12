package com.bobmukja.bobmukjaku.RoomDB

import androidx.room.*
import com.bobmukja.bobmukjaku.Model.RestaurantList

@Dao
interface RestaurantListDAO {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertRestaurant(restaurant: RestaurantList)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertAllRestaurant(vararg restaurant: RestaurantList)

    @Query("Select * from RestaurantList")
    fun getAllRecord(): List<RestaurantList>

    @Query("Delete from RestaurantList")
    fun deleteAllRestaurant()
}