package com.bobmukja.bobmukjaku.Model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class RestaurantList(
    @PrimaryKey var bizesId: String,
    @ColumnInfo var bizesNm: String,
    @ColumnInfo var indsMclsNm: String,
    @ColumnInfo var indsSclsNm: String,
    @ColumnInfo var lnoAdr: String,
    @ColumnInfo var lat: Double,
    @ColumnInfo var lon: Double
){
    override fun toString(): String {
        return bizesNm
    }
}
