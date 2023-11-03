package com.example.bobmukjaku.Dto

import com.fasterxml.jackson.annotation.JsonProperty

data class RateAndBgDto(
    @JsonProperty("rate")var rate: Int,
    @JsonProperty("bg")var bg: String)
