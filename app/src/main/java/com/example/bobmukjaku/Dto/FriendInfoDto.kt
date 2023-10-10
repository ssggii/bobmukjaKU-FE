package com.example.bobmukjaku.Dto

import com.fasterxml.jackson.annotation.JsonProperty

data class FriendInfoDto(
    @JsonProperty("friendUid")val friendUid: Long,
    @JsonProperty("friendNickname")val friendNickname: String,
    @JsonProperty("friendRate")var friendRate: Int?,
    @JsonProperty("friendProfileColor")var friendProfileColor: String?
)
