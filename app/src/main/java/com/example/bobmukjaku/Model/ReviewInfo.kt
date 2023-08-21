package com.example.bobmukjaku.Model

import com.fasterxml.jackson.annotation.JsonProperty

data class ReviewInfo(
    @JsonProperty("uid")val uid: Long,
    @JsonProperty("placeId")val placeId: String,
    @JsonProperty("imageName")val imageName: String,
    @JsonProperty("contents")val contents: String
)
