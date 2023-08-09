package com.example.bobmukjaku.Model

import retrofit2.Call
import retrofit2.http.*

interface ChatRoomService {
    // 모집방 개설
    @POST("/chatRoom")
    fun insertChatRoom(
        @Header("Authorization") authorization: String,
        @Query("roomName") roomName: String,
        @Query("date") date: String,
        @Query("startTime") startTime: String,
        @Query("endTime") endTime: String,
        @Query("kindOfFood") kindOfFood: String,
        @Query("total") total: Int
    ): Call<ChatRoom>

    // 모집방 전체 조회
    @GET("/chatRooms")
    fun selectOne(@Header("Authorization") authorization: String): Call<Member>

    // 사용자 조회 (모든 사용자)
    @GET("/members/info")
    fun setLists(@Header("Authorization") authorization: String): Call<List<ChatRoom>>

    // 필요한 다른 API 구현
}