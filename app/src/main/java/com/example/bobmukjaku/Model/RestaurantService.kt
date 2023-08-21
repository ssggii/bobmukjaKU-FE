package com.example.bobmukjaku.Model

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface RestaurantService {
    // 리뷰 등록
    @POST("/place/review")
    fun addReview(
        @Header("Authorization") authorization: String,
        @Body lists: List<ReviewInfo>
    ): Call<Void>

    // 스크랩 등록
    @POST("/place/scrap")
    fun addScrap(
        @Header("Authorization") authorization: String,
        @Body lists: List<ScrapInfo>
    ): Call<Void>

    // 필요한 다른 API 구현
}