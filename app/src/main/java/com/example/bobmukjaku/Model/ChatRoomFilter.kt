package com.example.bobmukjaku.Model

import com.fasterxml.jackson.annotation.JsonProperty

data class ChatRoomFilter(
    @JsonProperty("roomId") var roomId: Long?,
    @JsonProperty("roomName") var roomName: String?,
    @JsonProperty("meetingDate") var meetingDate: String?,
    @JsonProperty("startTime") var startTime: String?,
    @JsonProperty("endTime") var endTime: String?,
    @JsonProperty("kindOfFood") var kindOfFood: String?,
    @JsonProperty("total") var total: Int?,
    @JsonProperty("currentNum") var currentNum: Int?,
    @JsonProperty("hasFriend") var hasFriend: Int?
)
