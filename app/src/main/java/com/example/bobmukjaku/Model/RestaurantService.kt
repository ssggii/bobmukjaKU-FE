package com.example.bobmukjaku.Model

import retrofit2.Call
import retrofit2.http.*

interface RestaurantService {
    // 리뷰 등록
    @POST("/place/review")
    fun addReview(
        @Header("Authorization") authorization: String,
        @Body lists: ReviewInfo
    ): Call<Void>

    // 리뷰 삭제
    @DELETE("/place/review/info/{reviewId}")
    fun deleteReview(
        @Header("Authorization") authorization: String,
        @Path("reviewId") reviewId: Long
    ): Call<Void>

    // uid로 리뷰 조회
    @GET("/place/review/info/1/{uid}")
    fun getMyReview(
        @Header("Authorization") authorization: String,
        @Path("uid") uid: Long
    ): Call<List<ReviewResponse>>

    // 음식점 id로 리뷰 조회
    @GET("/place/review/info/2/{placeId}")
    fun getRestaurantReview(
        @Header("Authorization") authorization: String,
        @Path("placeId") placeId: String
    ): Call<List<ReviewResponse>>

    // 스크랩 등록
    @POST("/place/scrap")
    fun addScrap(
        @Header("Authorization") authorization: String,
        @Body lists: ScrapInfo
    ): Call<Void>

    // 스크랩 해제
    @POST("/place/scrap/remove")
    fun deleteScrap(
        @Header("Authorization") authorization: String,
        @Body lists: ScrapInfo
    ): Call<Void>

    // uid로 스크랩 조회
    @GET("/place/scrap/info/1/{uid}")
    fun getMyScrap(
        @Header("Authorization") authorization: String,
        @Path("uid") uid: Long
    ): Call<List<ScrapInfo>>

    // 음식점 스크랩 수 조회
    @GET("/place/scrap/count/{placeId}")
    fun getScrapCount(
        @Header("Authorization") authorization: String,
        @Path("placeId") placeId: String
    ): Call<Int>

    // 음식점 id로 스크랩 조회
    @GET("/place/scrap/info/2/{placeId}")
    fun getRestaurantScrap(
        @Header("Authorization") authorization: String,
        @Path("placeId") placeId: String
    ): Call<List<ScrapInfo>>

    // 필요한 다른 API 구현
}