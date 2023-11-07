package com.bobmukja.bobmukjaku.Model

data class UpdateScoreInfo(
    var participant: Member,
    var thumbUp: Boolean,
    var thumbDown: Boolean
)
