package com.example.bobmukjaku.Dto

import com.fasterxml.jackson.annotation.JsonProperty

data class LoginDto(
    @JsonProperty("password") var password: String?,
    @JsonProperty("username") var username: String?
)
