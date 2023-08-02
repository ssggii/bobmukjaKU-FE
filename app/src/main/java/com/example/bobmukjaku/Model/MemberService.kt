package com.example.bobmukjaku.Model

import android.content.Context
import android.content.SharedPreferences
import com.example.bobmukjaku.Dto.LoginDto
import com.example.bobmukjaku.R
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import retrofit2.http.*

interface MemberService {
    // 사용자 추가
    @POST("/signUp")
    fun insertMember(@Body member: SignUpRequest): Call<Void>

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
    fun login(@Body loginDto: LoginDto): Call<Void>

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
    private const val BASE_URL = "http://192.168.0.179:8081/" // 여기에 MariaDB 서버의 URL 넣기

    private val clientBuilder = OkHttpClient.Builder()
    private val loggingInterceptor = HttpLoggingInterceptor()

    //var clientBuilder = OkHttpClient.Builder()
    //var loggingInterceptor = HttpLoggingInterceptor()
    //loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
    //clientBuilder.addInterceptor(loggingInterceptor)

    private val retrofit: Retrofit by lazy {

        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
        clientBuilder.addInterceptor(loggingInterceptor)

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(JacksonConverterFactory.create())
            .client(clientBuilder.build())
            .build()
    }

    val memberService: MemberService by lazy {
        retrofit.create(MemberService::class.java)
    }
}

//앱이 종료되도 데이터(accessToken, refreshToken등)가 저장되도록
//SharedPreference클래스를 싱글통으로 정의
object SharedPreferences{
    private lateinit var sharedPreferences: SharedPreferences

    fun initSharedPreferences(context: Context){
        sharedPreferences = context
            .getSharedPreferences(
                context.getString(R.string.preference_file_key)
            , Context.MODE_PRIVATE)

    }

    fun getString(key: String, defValue: String?): String?{
        return sharedPreferences.getString(key, defValue)
    }

    fun putString(key: String, value: String?){
        sharedPreferences.edit().putString(key, value).apply()
    }

    fun remove(key: String){
        sharedPreferences.edit().remove(key).apply()
    }

    fun contains(key: String): Boolean{
        return sharedPreferences.contains(key)
    }
}
