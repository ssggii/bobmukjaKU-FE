package com.example.bobmukjaku.Dto

import com.fasterxml.jackson.annotation.JsonProperty

data class LoginDto(
    @JsonProperty("username") var username: String?,
    @JsonProperty("password") var password: String?
)
