package com.example.bobmukjaku.Model

import com.fasterxml.jackson.annotation.JsonProperty

data class AddChatRoomMember(
    @JsonProperty("roomId") var roomId: Long?,
    @JsonProperty("uid") var uid: Long?
)
