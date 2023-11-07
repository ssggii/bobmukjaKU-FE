package com.bobmukja.bobmukjaku.Dto

import com.bobmukja.bobmukjaku.Model.Member
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty


data class LoginResponseDto @JsonCreator constructor(

    @JsonProperty("accessToken")val accessToken: String,
    @JsonProperty("member") var member: Member

)
