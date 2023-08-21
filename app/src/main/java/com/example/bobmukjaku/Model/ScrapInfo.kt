package com.example.bobmukjaku.Model

import com.fasterxml.jackson.annotation.JsonProperty

data class ScrapInfo(
    @JsonProperty("uid")val uid: Long,
    @JsonProperty("placeId")val placeId: String
)
