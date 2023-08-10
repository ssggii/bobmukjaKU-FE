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

    // 모집방 참여자 추가
    @POST("/chatRoom/member")
    fun addMember(
        @Header("Authorization") authorization: String,
        @Body member: AddChatRoomMember
    ): Call<ServerBooleanResponse> // true(입장 성공), false(입장 실패)

    // 필요한 다른 API 구현
}