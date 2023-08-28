package com.example.bobmukjaku.Model

import com.fasterxml.jackson.annotation.JsonProperty

data class ReviewResponse(
    @JsonProperty("contents")val contents: String,
    @JsonProperty("imageUrl")val imageUrl: String
)
