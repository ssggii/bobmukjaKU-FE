package com.bobmukja.bobmukjaku.Model

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
    @GET("/chatRooms/info")
    fun setLists(@Header("Authorization") authorization: String): Call<List<ChatRoom>>

    // 모집방 참여자 추가
    @POST("/chatRoom/member")
    fun addMember(
        @Header("Authorization") authorization: String,
        @Body member: AddChatRoomMember
    ): Call<ServerBooleanResponse> // true(입장 성공), false(입장 실패)

    // 방 id로 모집방 조회
    @GET("/chatRoom/info/1/{roomId}")
    fun getRoomIdLists(
        @Header("Authorization") authorization: String,
        @Path("roomId") roomId: Long
    ): Call<ChatRoom>

    // uid로 참여 중인 모집방 조회
    @GET("/chatRoom/info/2/{uid}")
    fun getMyLists(
        @Header("Authorization") authorization: String,
        @Path("uid") uid: Long
    ): Call<List<ChatRoom>>

    // 최신순 정렬
    @GET("/chatRoom/filter/latest")
    fun getLatestLists(@Header("Authorization") authorization: String): Call<List<ChatRoom>>

    // 음식 종류 정렬
    @GET("/chatRoom/filter/1/{kindOfFood}")
    fun getFoodLists(
        @Header("Authorization") authorization: String,
        @Path("kindOfFood") kindOfFood: String
    ): Call<List<ChatRoom>>

    // 전체 필터링
    @POST("/chatRooms/filtered")
    fun filteredLists(
        @Header("Authorization") authorization: String,
        @Body filters: List<FilterInfo>
    ): Call<List<ChatRoomFilter>>

    // 필터링 리스트 조회
    @GET("/filter/info")
    fun getFilter(
        @Header("Authorization") authorization: String
    ) : Call<List<FilterInfo>>

    // 모집방 나가기
    @POST("/chatRoom/member/exit")
    fun exitChatRoom(
        @Header("Authorization") authorization: String,
        @Body member: AddChatRoomMember
    ): Call<ServerBooleanResponse> // true(퇴장 성공), false(퇴장 실패)

    // 필요한 다른 API 구현
}