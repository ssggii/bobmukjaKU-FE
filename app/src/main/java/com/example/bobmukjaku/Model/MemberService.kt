package com.example.bobmukjaku.Model

//import android.os.Build.VERSION_CODES.R
import android.content.Context
import android.content.SharedPreferences
import com.example.bobmukjaku.Dto.*
import com.example.bobmukjaku.R
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import retrofit2.http.*

interface MemberService {
    // 사용자 추가
    @POST("/signUp")
    fun insertMember(@Body member: SignUpRequest): Call<Void>

    // 사용자 조회 (단일 사용자)
    @GET("/member/info")
    fun selectOne(@Header("Authorization") authorization: String): Call<Member>

    // 사용자 조회 (모든 사용자)
    @GET("/members/info")
    fun selectAll(): Call<List<Member>>

    // 사용자 수정
    @PUT("/member/info")
    fun updateMember(
        @Header("Authorization") authorization: String,
        @Body requestBody: Map<String, String>
    ): Call<Void>

    //로그인 없는 사용자 수정
    @PUT("/resetPassword")
    fun updateMemberWithoutLogin(@Body passwordUpdateDto : UpdatePasswordDto) : Call<Void>

    // rate 업데이트
    @PUT("/member/info/rate")
    fun rateUpdate(
        @Header("Authorization") authorization: String,
        @Body rateUpdateDto: RateUpdateDto): Call<Void>

    // 비밀번호 재설정
    @PUT("/member/info/password")
    fun updatePassword(
        @Header("Authorization") authorization: String,
        @Body member: UpdatePassword
    ): Call<Void>

    // 사용자 삭제
    @DELETE("member/info")
    fun deleteMember(
        @Header("Authorization") authorization: String,
        @Body requestBody: Map<String, String>
    ): Call<Void>

    // 로그인
    @POST("/login")
    //fun login(@Body loginDto: LoginDto, @Header("registrationKey") registrationKey: String): Call<Void>
    fun login(@Body loginDto: LoginDto): Call<Void>

    // 시간표 저장
    @POST("/member/info/timeTable")
    fun saveTimeTable(
        @Header("Authorization") authorization: String,
        @Body requestBody: List<TimeBlock>
    ): Call<Void>

    // 시간표 조회
    @GET("/timeTable")
    fun getTimeTable(
        @Header("Authorization") authorization: String
    ): Call<List<TimeBlock>>

    // 인증날짜만료 체크
    @GET("certificatedAtCheck")
    fun certificatedAtCheck(@Header("accessToken") accessToken: String): Call<Member>

    //재학생 인증
    @GET("mailAuth")
    fun RequestMailAuth(@Query("email") email:String):Call<HashedAuthCode>

    //메시지 전송
    @PUT("message")
    fun sendMessage(@Header("authorization") authorization: String, @Body md: ChatModel): Call<Unit>

    //방id로 모든 참여자 조회
    @GET("/chatRoom/joiners/{roomId}")
    fun getParticipantsInRoom(
        @Header("authorization") authorization: String,
        @Path("roomId") roomId: Long
    ): Call<List<Member>>

    //로그인없이 사용자의 rate와 배경색 get
    @GET("/member/name_rate_bg/{uid}")
    fun getNameRateBg(@Path("uid") uid: Long): Call<NameRateBgDto>

    //로그아웃
    @POST("/auth/logout")
    fun logout(@Header("authorization") authorization: String): Call<Unit>

    // 필요한 다른 API 구현

}

object RetrofitClient {
    //private const val BASE_URL = "https://4a76-124-58-128-62.ngrok-free.app" // 여기에 서버 URL 넣기
    private const val BASE_URL = "http://192.168.184.74:8080" // 여기에 서버 URL 넣기

    private fun provideOkHttpClient(interceptor: ContentTypeInterceptor): OkHttpClient
            = OkHttpClient.Builder().run {
        addInterceptor(interceptor)
        build()
    }


    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(JacksonConverterFactory.create())
            .client(provideOkHttpClient(ContentTypeInterceptor()))
            .build()
    }

    val memberService: MemberService by lazy {
        retrofit.create(MemberService::class.java)
    }

    val chatRoomService: ChatRoomService by lazy {
        retrofit.create(ChatRoomService::class.java)
    }

    val restaurantService: RestaurantService by lazy {
        retrofit.create(RestaurantService::class.java)
    }

    val friendService: FriendService by lazy {
        retrofit.create(FriendService::class.java)
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
