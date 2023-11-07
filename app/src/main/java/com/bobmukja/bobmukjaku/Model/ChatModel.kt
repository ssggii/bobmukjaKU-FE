package com.bobmukja.bobmukjaku.Model

import com.fasterxml.jackson.annotation.JsonProperty

data class ChatModel(
    @JsonProperty val message:String?,
    @JsonProperty val senderUid:Long?,
    @JsonProperty val senderName:String?,
    @JsonProperty val time:Long?,
    @JsonProperty val shareMessage:Boolean?,
    @JsonProperty val chatRoomId:Long?,
    @JsonProperty val profanity:Boolean)
