package com.bobmukja.bobmukjaku.Model

import com.fasterxml.jackson.annotation.JsonProperty

data class ScrapPost(
    @JsonProperty("uid")val uid: Long,
    @JsonProperty("placeId")val placeId: String,
    @JsonProperty("placeName")val placeName: String
)
