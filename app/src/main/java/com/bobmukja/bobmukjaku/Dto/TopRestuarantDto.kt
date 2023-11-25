package com.bobmukja.bobmukjaku.Dto

import com.fasterxml.jackson.annotation.JsonProperty

data class TopRestuarantDto(
    @JsonProperty("placeId")val placeId: String,
    )
