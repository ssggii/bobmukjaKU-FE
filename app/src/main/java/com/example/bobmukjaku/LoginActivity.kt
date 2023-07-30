package com.example.bobmukjaku

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.bobmukjaku.Dto.LoginDto
import com.example.bobmukjaku.Dto.LoginResponseDto
import com.example.bobmukjaku.Model.Member
import com.example.bobmukjaku.Model.RetrofitClient
import com.example.bobmukjaku.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class LoginActivity : AppCompatActivity() {
    lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

    lateinit var sharedPreference: SharedPreferences

    private var toast: Toast? = null
    private var toast2: Toast? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityLoginBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)


        initSharedPreference()
        //sharedPreference.edit().remove("accessToken").apply()
        initLayout()
        autoLogin()


    }

    private fun autoLogin() {
        //shared preference에서 accessToken을 꺼내와서 값이 있으면 자동로그인
        if(sharedPreference!!.contains("accessToken")){

            //자동로그인하기 전 재학생 인증 만료 체크
            certificatedAtCheck()

            //만료 되지 않았으면 main으로
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    private fun certificatedAtCheck() {
        //서버에서 인증날짜를 비교해서 재학생인증이 만료됐는지 체크

        //val request = service.certificatedAtCheck(sharedPreference?.getString("accessToken", "")!!)
        val request = RetrofitClient.memberService.certificatedAtCheck(sharedPreference.getString("accessToken", "")?:"")
        CoroutineScope(Dispatchers.IO).launch {
            request.enqueue(object: Callback<Member>{
                @RequiresApi(Build.VERSION_CODES.O)
                override fun onResponse(call: Call<Member>, response: Response<Member>) {
                    //서버에서 인증날짜 값을 받아왔으므로, 인증날짜로부터 1년이 지났는지 체크하여 인증 만료 여부 결정

                    Log.i("kim", "test")

                    if(response.isSuccessful) {
                        val certificatedAtFromServerString =
                            response.body()?.certificatedAt ?: return//인증날짜가 null -> 재학생 인증 한 적이 없다.
                        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                        val certificatedAtFromServer =
                            LocalDate.parse(certificatedAtFromServerString, formatter)
                        //Log.i("kim", certificatedAtFromServer.toString())
                        //Log.i("kim", LocalDate.now().toString())

                        val daysUntilCertificatedAt = certificatedAtFromServer.toEpochDay()
                        val daysUntilNow = LocalDate.now().toEpochDay()

                        if(daysUntilNow - daysUntilCertificatedAt >= 365){
                            //인증한지 1년이 지났으므로 재학생 인증 화면으로
                            val intent = Intent(this@LoginActivity, Join2Activity::class.java)
                            intent.putExtra("alreadyJoin", true)
                            startActivity(intent)
                        }
                    }
                }

                override fun onFailure(call: Call<Member>, t: Throwable) {
                    Log.i("kim", "error: ${t.message}")
                }

            })
        }

    }


    private fun initSharedPreference(){
        sharedPreference = applicationContext
            .getSharedPreferences(
                getString(R.string.preference_file_key)
                , Context.MODE_PRIVATE)
    }

    private fun initLayout() {
        binding.apply {

            //로그인 버튼 클릭
            loginBtn.setOnClickListener {
                val id = loginId.text.toString()
                val passwd = loginPasswd.text.toString()


                if (id.isEmpty() && passwd.isEmpty()) {
                    Toast.makeText(this@LoginActivity, "아이디와 비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
                } else if (id.isEmpty()) {
                    Toast.makeText(this@LoginActivity, "아이디를 입력해주세요.", Toast.LENGTH_SHORT).show()
                } else if (passwd.isEmpty()) {
                    Toast.makeText(this@LoginActivity, "비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
                } else {
                    //이곳에서 아이디, 패스워드를 서버에 전송하여 로그인
                    //로그인 성공 시 JWT access token + member객체(access token은 앱을 삭제하지 않은 한 유지되도록 shared preference에 저장)
                    //로그인 실패 시 ?


                    //val request = service.login(LoginDto(id, passwd))
                    val request = RetrofitClient.memberService.login(LoginDto(id,passwd))
                    CoroutineScope(Dispatchers.IO).launch {
                        request.enqueue(object: Callback<LoginResponseDto> {
                            override fun onResponse(
                                call: Call<LoginResponseDto>,
                                response: Response<LoginResponseDto>
                            ) {
                                val body = response.body()
                                val accessToken = body?.accessToken ?: null
                                val member = body?.member?: null

                                //Log.i("kim", accessToken ?: "null")
                                //Log.i("kim", member?.toString() ?: "null")
                                when{
                                    (accessToken != null && member != null)->{
                                        //로그인 성공, shared preference에 access token을 저장한다.
                                        Log.i("kim", accessToken)
                                        sharedPreference.edit().putString("accessToken", accessToken).apply()

                                        //인증날짜 만료 체크
                                        certificatedAtCheck()

                                        //메인화면으로 전환
                                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                                        startActivity(intent)
                                    }
                                    else->{
                                        //로그인 실패
                                        toast?.cancel()
                                        toast = Toast.makeText(this@LoginActivity
                                            , "로그인에 실패하였습니다.",
                                            Toast.LENGTH_SHORT)
                                        toast?.show()
                                    }
                                }
                            }

                            override fun onFailure(call: Call<LoginResponseDto>, t: Throwable) {

                                Log.i("kim", t.message!!)

                                toast2?.cancel()
                                toast = Toast.makeText(this@LoginActivity,
                                    t.message,
                                    Toast.LENGTH_SHORT)
                                toast?.show()
                            }

                        })
                    }
                }
            }

            //회원가입 버튼 클릭
            joinBtn.setOnClickListener {
                /*val intent = Intent(this@LoginActivity, JoinActivity::class.java)
                startActivity(intent)*/
                val intent = Intent(this@LoginActivity, Join2Activity::class.java)
                startActivity(intent)
            }
        }
    }
    //파이어베이스로 로그인을 구현한 파트
    /*private fun initLayout() {
        binding.apply {
            loginBtn.setOnClickListener {
                val id = loginId.text.toString()
                val passwd = loginPasswd.text.toString()

                auth = Firebase.auth

                if (id.isEmpty() && passwd.isEmpty()) {
                    Toast.makeText(this@LoginActivity, "아이디와 비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
                } else if (id.isEmpty()) {
                    Toast.makeText(this@LoginActivity, "아이디를 입력해주세요.", Toast.LENGTH_SHORT).show()
                } else if (passwd.isEmpty()) {
                    Toast.makeText(this@LoginActivity, "비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
                } else {
                    auth.signInWithEmailAndPassword(id, passwd)
                        .addOnCompleteListener(this@LoginActivity) { task ->
                            if (task.isSuccessful) {
                                // Sign in success, update UI with the signed-in user's information

                                //데이터베이스에 user정보를 넣어줘야함
                                val intent = Intent(this@LoginActivity, MainActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                            } else {
                                // If sign in fails, display a message to the user.
                                //updateUI(null)
                                Toast.makeText(this@LoginActivity, "login 실패", Toast.LENGTH_LONG).show()
                            }
                        }
                }
            }

            joinBtn.setOnClickListener {
                val intent = Intent(this@LoginActivity, Join2Activity::class.java)
                startActivity(intent)
            }
        }
    }*/
}