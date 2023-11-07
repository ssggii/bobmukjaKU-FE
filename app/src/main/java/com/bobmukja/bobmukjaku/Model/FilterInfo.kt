package com.bobmukja.bobmukjaku.Model

import com.fasterxml.jackson.annotation.JsonProperty

data class FilterInfo(
    @JsonProperty("filterType")val filterType: String,
    @JsonProperty("filterValue")val filterValue: String
)
