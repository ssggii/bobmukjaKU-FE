package com.bobmukja.bobmukjaku.Model

import com.fasterxml.jackson.annotation.JsonProperty

data class ReviewResponse(
    @JsonProperty("uid")val uid: Long,
    @JsonProperty("placeId")val placeId: String,
    @JsonProperty("placeName")val placeName: String,
    @JsonProperty("contents")val contents: String,
    @JsonProperty("imageUrl")val imageUrl: String
)
