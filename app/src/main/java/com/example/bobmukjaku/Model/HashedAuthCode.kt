package com.example.bobmukjaku.Model

import com.fasterxml.jackson.annotation.JsonProperty

data class HashedAuthCode(
    @JsonProperty("hashedAuthCode")val hashedAuthCode: String
    )
