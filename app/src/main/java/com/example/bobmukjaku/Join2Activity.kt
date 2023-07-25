package com.example.bobmukjaku

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bobmukjaku.Hash.Sha256
import com.example.bobmukjaku.Model.HashedAuthCode
import com.example.bobmukjaku.databinding.ActivityJoin2Binding
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

class Join2Activity : AppCompatActivity() {
    lateinit var binding: ActivityJoin2Binding
    private lateinit var auth: FirebaseAuth
    private lateinit var authStateListener: FirebaseAuth.AuthStateListener

    var hashedAuthCodeFromServer:String = ""
    val sha256 = Sha256()

    //테스트 깃
    private val BASE_URL = "http://192.168.219.106:8080/"
    lateinit var retrofit: Retrofit

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityJoin2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        /*auth = Firebase.auth
        initLayout()
        setupAuthStateListener()*/


        initRetroFit()
        initLayout()

    }

    private fun initLayout() {
        binding.sendBtn.setOnClickListener {
            //서버에게 이메일인증을 요청

            val service = retrofit.create(MailAuthApi::class.java)
            val repos = service.RequestMailAuth("kimdm4638@naver.com")
            CoroutineScope(Dispatchers.IO).launch {
                repos.enqueue(object : Callback<HashedAuthCode>{
                    override fun onResponse(
                        call: Call<HashedAuthCode>,
                        response: Response<HashedAuthCode>
                    ) {
                        Log.i("kim", response.body()!!.hashedAuthCode)
                        hashedAuthCodeFromServer = response.body()!!.hashedAuthCode
                    }

                    override fun onFailure(call: Call<HashedAuthCode>, t: Throwable) {
                        TODO("Not yet implemented")
                    }
                })
            }
        }

        binding.join2Button.setOnClickListener {
            //서버에서 인증코드 해시값을 받았다는 전제
            //입력한 인증코드의 해시값과 서버로부터의 해시값을 비교 -> 일치하면 mainActivity(메인화면)으로

            //먼저 인증코드 입력폼의 숫자들을 모아서 하나의 문자열로 만든다.
            var authCodeFromUser = ""
            authCodeFromUser= authCodeFromUser
                .plus(binding.num1.text.toString())
                .plus(binding.num2.text.toString())
                .plus(binding.num3.text.toString())
                .plus(binding.num4.text.toString())
                .plus(binding.num5.text.toString())
                .plus(binding.num6.text.toString())

            Log.i("kim", authCodeFromUser)

            if(authCodeFromUser.length != 6){
                Toast.makeText(this, "6자리의 인증코드를 제대로 입력하세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            val hashedAuthCodeFromUser = sha256.encrypt(authCodeFromUser)
            if(hashedAuthCodeFromServer != ""){
                when{
                    (hashedAuthCodeFromUser == hashedAuthCodeFromServer)->{
                        Log.i("kim", "일치합니다.")
                        //메인화면으로 전환하는 코드...
                    }
                    else->{
                        Log.i("kim", "불일치합니다.")
                    }
                }
            }
        }

        //인증코드 입력폼 관련 리스너 초기화 -> 숫자입력하면 자동으로 다음 EditText로 포커스가 가도록
        binding.num1.setOnFocusChangeListener { v, hasFocus ->
            if(hasFocus){
                binding.num1.setText("")
            }
        }
        binding.num1.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                if(binding.num1.length() == 1){
                    binding.num2.requestFocus()
                }
            }
        })
        binding.num2.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                if(binding.num2.length() == 1){
                    binding.num3.requestFocus()
                }
            }
        })
        binding.num2.setOnFocusChangeListener { v, hasFocus ->
            if(hasFocus){
                binding.num2.setText("")
            }
        }
        binding.num3.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                if(binding.num3.length() == 1){
                    binding.num4.requestFocus()
                }
            }
        })
        binding.num3.setOnFocusChangeListener { v, hasFocus ->
            if(hasFocus){
                binding.num3.setText("")
            }
        }
        binding.num4.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                if(binding.num4.length() == 1){
                    binding.num5.requestFocus()
                }
            }
        })
        binding.num4.setOnFocusChangeListener { v, hasFocus ->
            if(hasFocus){
                binding.num4.setText("")
            }
        }
        binding.num5.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                if(binding.num5.length() == 1){
                    binding.num6.requestFocus()
                }
            }
        })
        binding.num5.setOnFocusChangeListener { v, hasFocus ->
            if(hasFocus){
                binding.num5.setText("")
            }
        }
        binding.num6.setOnFocusChangeListener { v, hasFocus ->
            if(hasFocus){
                binding.num6.setText("")
            }
        }
    }

    //Retrofit사용을 위한 초기화
    private fun initRetroFit() {
        retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(JacksonConverterFactory.create())
            .build()
    }

    //Retrofit에서 요청을 위한 함수를 정의하는 인터페이스
    interface MailAuthApi{
        @GET("mailAuth")
        fun RequestMailAuth(@Query("email") email:String):Call<HashedAuthCode>
    }


    //지흔님 파트 - firebase로 이메일 인증 구현
    /*private fun initLayout() {
        val user = Firebase.auth.currentUser

        binding.sendBtn.setOnClickListener {
            if (binding.emailSend.text.contains("인증 메일이 발송되었습니다")) {
                // 인증 완료 여부 확인
                val isEmailVerified = user?.isEmailVerified

                if (isEmailVerified == true) {
                    binding.emailSend.text = "인증이 완료되었습니다"
                } else {
                    binding.emailSend.text = "인증이 완료되지 않았습니다\n추후 추가"
                }
            } else {
                user!!.sendEmailVerification()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d(TAG, "Email 전송")
                            binding.emailSend.text =
                                "인증 메일이 발송되었습니다\n메일함을 확인해 재학생 인증을 완료하고 다시 버튼을 눌러주세요"
                        }
                    }
            }
        }
    }

    private fun setupAuthStateListener() {
        authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser

            // 인증 완료 여부 확인
            val isEmailVerified = user?.isEmailVerified

            if (isEmailVerified == true) {
                binding.emailSend.text = "인증이 완료되었습니다"
            } else {
                binding.emailSend.text = "인증이 완료되지 않았습니다\n추후 추가"
            }
        }
    }

    override fun onStart() {
        super.onStart()
        auth.addAuthStateListener(authStateListener)

        // 인증 완료 여부 초기 확인
        val user = auth.currentUser
        val isEmailVerified = user?.isEmailVerified

        if (isEmailVerified == true) {
            binding.emailSend.text = "인증이 완료되었습니다"
        } else {
            binding.emailSend.text = "인증이 완료되지 않았습니다\n추후 추가"
        }
    }

    override fun onStop() {
        super.onStop()
        auth.removeAuthStateListener(authStateListener)
    }*/
}