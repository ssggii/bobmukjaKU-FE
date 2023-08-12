package com.example.bobmukjaku.Model

import com.fasterxml.jackson.annotation.JsonProperty

data class ChatModel(
    @JsonProperty val message:String?,
    @JsonProperty val senderUid:Long?,
    @JsonProperty val senderName:String?,
    @JsonProperty val time:Long?,
    @JsonProperty val isShareMessage:Boolean?,
    @JsonProperty val chatRoomId:Long?)
