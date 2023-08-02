package com.example.bobmukjaku.Model

import com.fasterxml.jackson.annotation.JsonProperty

data class SignUpRequest(
    @JsonProperty("memberEmail") var memberEmail: String?,
    @JsonProperty("password") var password: String?,
    @JsonProperty("nickname") var nickname: String?
)