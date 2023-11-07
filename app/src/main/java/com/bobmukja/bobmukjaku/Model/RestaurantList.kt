package com.bobmukja.bobmukjaku.Model

data class RestaurantList(
    var bizesId: String,
    var bizesNm: String,
    var indsMclsNm: String,
    var indsSclsNm: String,
    var lnoAdr: String,
    var lat: Double,
    var lon: Double
){
    override fun toString(): String {
        return bizesNm
    }
}
