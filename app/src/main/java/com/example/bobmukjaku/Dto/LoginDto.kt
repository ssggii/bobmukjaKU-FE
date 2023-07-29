package com.example.bobmukjaku.Dto

import com.fasterxml.jackson.annotation.JsonProperty

data class LoginDto(

    @JsonProperty
    var email: String,
    @JsonProperty
    var password: String
)
