package com.example.bobmukjaku.Model

import com.fasterxml.jackson.annotation.JsonProperty


data class Member(

    @JsonProperty("uid")var uid: Long?,
    @JsonProperty("memberEmail")var memberEmail: String?,
    @JsonProperty("memberPassword")var memberPassword: String?,
    @JsonProperty("memberNickName")var memberNickName: String?,
    @JsonProperty("certificatedAt")var certificatedAt: String?,
    @JsonProperty("rate")var rate: Int?,
    @JsonProperty("profileColor")var profileColor: String?,
    //var role: Role,
) : java.io.Serializable