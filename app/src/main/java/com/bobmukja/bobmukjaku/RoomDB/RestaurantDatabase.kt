package com.bobmukja.bobmukjaku.RoomDB

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.bobmukja.bobmukjaku.Model.RestaurantList

@Database(entities = [RestaurantList::class], version = 1)
abstract class RestaurantDatabase : RoomDatabase(){
    abstract fun restaurantListDao():RestaurantListDAO

    companion object{
        private var INSTANCE: RestaurantDatabase? = null

        fun getDatabase(context: Context): RestaurantDatabase{
            val tempInstance = INSTANCE

            if(tempInstance != null){
                return tempInstance
            }

            val instance = Room.databaseBuilder(
                context,
                RestaurantDatabase::class.java,
                "restaurantlistdb"
            ).build()

            INSTANCE = instance
            return instance
        }
    }
}