package com.example.bobmukjaku.Model

import com.fasterxml.jackson.annotation.JsonProperty

data class InsertChatRoomRequest(
    @JsonProperty("roomName") var roomName: String?,
    @JsonProperty("date") var date: String?,
    @JsonProperty("startTime") var startTime: String?,
    @JsonProperty("endTime") var endTime: String?,
    @JsonProperty("kindOfFood") var kindOfFood: String?,
    @JsonProperty("total") var total: Int?
)
