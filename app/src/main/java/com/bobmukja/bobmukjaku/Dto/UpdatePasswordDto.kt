package com.bobmukja.bobmukjaku.Dto

import com.fasterxml.jackson.annotation.JsonProperty

data class UpdatePasswordDto(
    @JsonProperty("newPassword") var newPassword: String?,
    @JsonProperty("username") var username: String?
)
