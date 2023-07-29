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
)

fun Member.getUid(): Long? {
    return this.uid
}

fun Member.getMemberEmail(): String? {
    return this.memberEmail
}

fun Member.getMemberPassword(): String? {
    return this.memberPassword
}

fun Member.getMemberNickName(): String? {
    return this.memberNickName
}

fun Member.getRate(): Int? {
    return this.rate
}

fun Member.getProfileColor(): String? {
    return this.profileColor
}