package com.bobmukja.bobmukjaku.Model

import com.fasterxml.jackson.annotation.JsonProperty

data class UpdatePassword(
    @JsonProperty("tobePassword") var tobePassword: String?,
    @JsonProperty("checkPassword") var checkPassword: String?
)
