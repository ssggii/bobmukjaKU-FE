package com.example.bobmukjaku.Model

import com.example.bobmukjaku.Dto.LoginDto
import com.example.bobmukjaku.Dto.LoginResponseDto
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import retrofit2.http.*

interface MemberService {
    // 사용자 추가
    @POST("/signUp")
    fun insertMember(@Body request: SignUpRequest): Call<Void>

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

    //로그인
    @POST("/login")
    fun login(@Body loginDto: LoginDto): Call<LoginResponseDto>

    //인증날짜만료 체크
    @GET("certificatedAtCheck")
    fun certificatedAtCheck(@Header("accessToken") accessToken: String): Call<Member>

    //재학생 인증
    @GET("mailAuth")
    fun RequestMailAuth(@Query("email") email:String):Call<HashedAuthCode>

    // 필요한 다른 API 구현
}

object RetrofitClient {
    //private const val BASE_URL = "http://your-maria-db-server-url/api/" // 여기에 MariaDB 서버의 URL 넣기
    private const val BASE_URL = "http://192.168.219.103:8081/" // 여기에 MariaDB 서버의 URL 넣기

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(JacksonConverterFactory.create())
            .build()
    }

    val memberService: MemberService by lazy {
        retrofit.create(MemberService::class.java)
    }
}
