package com.example.bobmukjaku.Model

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

interface MemberService {
    // 사용자 추가
    @POST("insert")
    fun insertMember(@Body member: Member): Call<Void>

    // 사용자 조회 (단일 사용자)
    @GET("select/{uid}")
    fun selectOne(@Path("uid") uid: Long): Call<Member>

    // 사용자 조회 (모든 사용자)
    @GET("select")
    fun selectAll(): Call<List<Member>>

    // 사용자 수정
    @PUT("update/{uid}")
    fun updateMember(@Path("uid") uid: Long, @Body member: Member): Call<Void>

    // 사용자 삭제
    @DELETE("delete/{uid}")
    fun deleteMember(@Path("uid") uid: Long): Call<ResponseBody>

    // 필요한 다른 API 구현
}

object RetrofitClient {
    private const val BASE_URL = "http://your-maria-db-server-url/api/" // 여기에 MariaDB 서버의 URL 넣기

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val memberService: MemberService by lazy {
        retrofit.create(MemberService::class.java)
    }
}
