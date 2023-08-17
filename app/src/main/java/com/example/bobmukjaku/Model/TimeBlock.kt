package com.example.bobmukjaku.Model

import com.fasterxml.jackson.annotation.JsonProperty

data class TimeBlock(
    @JsonProperty("dayOfWeek")val dayOfWeek: String,
    @JsonProperty("time")val time: String
)
