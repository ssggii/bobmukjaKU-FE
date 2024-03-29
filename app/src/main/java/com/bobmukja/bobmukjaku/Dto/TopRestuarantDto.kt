package com.bobmukja.bobmukjaku.Dto

import com.fasterxml.jackson.annotation.JsonProperty

data class TopRestuarantDto(
    @JsonProperty("placeId")val placeId: String,
    @JsonProperty("placeName")val placeName: String,
    @JsonProperty("scrapCount")val scrapCount: Int,
    @JsonProperty("reviewCount")val reviewCount: Int,
    )
