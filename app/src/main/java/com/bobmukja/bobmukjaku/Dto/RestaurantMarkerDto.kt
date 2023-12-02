package com.bobmukja.bobmukjaku.Dto

import com.fasterxml.jackson.annotation.JsonProperty

data class RestaurantMarkerDto(
    @JsonProperty("placeId")val placeId: String,
    @JsonProperty("placeName")val placeName: String,
)
