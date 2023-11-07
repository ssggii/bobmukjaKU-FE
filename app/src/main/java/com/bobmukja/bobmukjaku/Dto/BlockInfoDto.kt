package com.bobmukja.bobmukjaku.Dto

import com.fasterxml.jackson.annotation.JsonProperty

data class BlockInfoDto(
    @JsonProperty("blockUid")val blockUid: Long,
    @JsonProperty("blockNickname")val blockNickname: String,
    @JsonProperty("blockRate")var blockRate: Int?,
    @JsonProperty("blockProfileColor")var blockProfileColor: String?
)
