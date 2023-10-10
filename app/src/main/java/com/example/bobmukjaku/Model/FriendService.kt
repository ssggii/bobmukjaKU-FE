package com.example.bobmukjaku.Model

import com.example.bobmukjaku.Dto.BlockInfoDto
import com.example.bobmukjaku.Dto.FriendInfoDto
import com.example.bobmukjaku.Dto.FriendUpdateDto
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface FriendService {
    // 친구 등록
    @POST("/friend/registering")
    fun registerFriend(
        @Header("Authorization") authorization: String,
        @Body friendUpdateDto: FriendUpdateDto
    ): Call<Unit>

    // 차단 등록
    @POST("/block/registering")
    fun blockFriend(
        @Header("Authorization") authorization: String,
        @Body friendUpdateDto: FriendUpdateDto
    ): Call<Unit>

    // 친구 해제
    @POST("/friend/removing")
    fun removeFriend(
        @Header("Authorization") authorization: String,
        @Body friendUpdateDto: FriendUpdateDto
    ): Call<Void>

    // 차단 해제
    @POST("/block/removing")
    fun removeBlock(
        @Header("Authorization") authorization: String,
        @Body friendUpdateDto: FriendUpdateDto
    ): Call<Void>

    // 내 친구 목록 조회
    @GET("/friend/all")
    fun getFriendList(@Header("Authorization") authorization: String): Call<List<FriendInfoDto>>

    // 내 친구 목록 조회
    @GET("/block/all")
    fun getBlockList(@Header("Authorization") authorization: String): Call<List<BlockInfoDto>>
}