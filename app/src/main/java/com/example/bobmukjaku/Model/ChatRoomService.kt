package com.example.bobmukjaku.Model

import retrofit2.Call
import retrofit2.http.*

interface ChatRoomService {
    // 모집방 개설
    @POST("/chatRoom")
    fun insertChatRoom(
        @Header("Authorization") authorization: String,
        @Body member: InsertChatRoomRequest
    ): Call<ChatRoom>

    // 모집방 전체 조회
    @GET("/chatRooms")
    fun setLists(@Header("Authorization") authorization: String): Call<List<ChatRoom>>


    // 필요한 다른 API 구현
}