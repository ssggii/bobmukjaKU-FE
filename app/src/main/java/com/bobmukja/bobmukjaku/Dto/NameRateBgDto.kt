package com.bobmukja.bobmukjaku.Dto

import com.fasterxml.jackson.annotation.JsonProperty

data class NameRateBgDto(
    @JsonProperty("name")var name: String,
    @JsonProperty("rate")var rate: Int,
    @JsonProperty("bg")var bg: String)
