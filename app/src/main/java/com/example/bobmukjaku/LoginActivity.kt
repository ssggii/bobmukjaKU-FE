package com.example.bobmukjaku

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.bobmukjaku.Dto.LoginDto
import com.example.bobmukjaku.Model.Member
import com.example.bobmukjaku.Model.RetrofitClient
import com.example.bobmukjaku.Model.SharedPreferences
import com.example.bobmukjaku.databinding.ActivityLoginBinding
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

    private var toast: Toast? = null
    private var toast2: Toast? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityLoginBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        SharedPreferences.initSharedPreferences(applicationContext)
        SharedPreferences.remove("accessToken")
        SharedPreferences.remove("refreshToken")
        initLayout()
        autoLogin()


    }

    private fun autoLogin() {
        //shared preference에서 accessToken을 꺼내와서 값이 있으면 자동로그인
        if(SharedPreferences.contains("accessToken")){

            //자동로그인하기 전 재학생 인증 만료 체크
            certificatedAtCheck()

            //만료 되지 않았으면 main으로
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    private fun certificatedAtCheck() {
        //서버에서 인증날짜를 비교해서 재학생인증이 만료됐는지 체크

        val request = RetrofitClient
            .memberService
            .certificatedAtCheck(
                SharedPreferences
                    .getString("accessToken", "")?:"")
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

                        //날짜차이가 1년 이상이면 재학생 인증 화면으로
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


    private fun initLayout() {

        binding.apply {

            //최근 로그인한 이메일 자동입력
            if(SharedPreferences.contains("recentLoginId")){
                loginId.setText(SharedPreferences.getString("recentLoginId",""))
            }
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
                    //로그인 성공(200) 시 헤더에서 accessToken, refreshToken을 꺼내와서 SharedPreferences에 저장
                    //로그인 실패(그외) 시 "로그인 실패"메시지 출력


                    val request = RetrofitClient.memberService.login(LoginDto(id,passwd))
                    //CoroutineScope(Dispatchers.IO).launch {
                        request.enqueue(object: Callback<Void> {
                            override fun onResponse(
                                call: Call<Void>,
                                response: Response<Void>
                            ) {
                                Log.i("success", response.raw().toString())

                                //응답상태코드를 보고 로그인에 성공했으면
                                //헤더에서 accessToken, refreshToken을 꺼내온다.
                                if(response.isSuccessful){

                                    when(response.code()){
                                        200->{
                                            /*로그인에 성공
                                            헤더에서 accessToken, refreshToken을 가져와서
                                            내부DB인 SharedPreferences에 저장
                                            이후 요청을 보낼때마다 accessToken을 꺼내어와서 헤더에 붙일 수 있도록
                                             */

                                            val headers = response.headers()
                                            val accessToken = headers["Authorization"]
                                            val refreshToken = headers["Authorization-refresh"]

                                            SharedPreferences.putString("accessToken", accessToken)
                                            SharedPreferences.putString("refreshToken", refreshToken)

                                            //다음에 로그인할 때, id(email)입력창에 최근에 로그인한 이메일이 자동입력되도록
                                            //여기서 SharedPreferences에 로그인한 이메일을 저장
                                            SharedPreferences.putString("recentLoginId", id)

//                                            val a = SharedPreferences.getString("accessToken","null") ?: "null"
//                                            val r = SharedPreferences.getString("refreshToken","null") ?: "null"
//                                            Log.i("kim", a?:"null-accessToken")
//                                            Log.i("kim", r?:"null-refreshToken")

                                            //인증날짜 만료되었는지 체크 후
                                            certificatedAtCheck()

                                            //메인화면으로 전환
                                            val intent = Intent(this@LoginActivity, MainActivity::class.java)
                                            startActivity(intent)

                                            Toast.makeText(this@LoginActivity, "로그인 성공", Toast.LENGTH_SHORT).show()
                                        }
                                        else->{
                                            //200 이외의 상태코드는 모두 로그인 실패
                                            Toast.makeText(this@LoginActivity, "로그인 실패", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            }

                            override fun onFailure(call: Call<Void>, t: Throwable) {

                                Log.i("error", "error")
                                Log.i("kim1", t.message!!)

                                toast2?.cancel()
                                toast = Toast.makeText(this@LoginActivity,
                                    t.message,
                                    Toast.LENGTH_SHORT)
                                toast?.show()
                            }

                        })
                    //}
                }
            }

            //회원가입 버튼 클릭
            joinBtn.setOnClickListener {
                /*val intent = Intent(this@LoginActivity, JoinActivity::class.java)
                startActivity(intent)*/
                val intent = Intent(this@LoginActivity, JoinActivity::class.java)
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