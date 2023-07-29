package com.example.bobmukjaku

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import com.example.bobmukjaku.Model.Member
import com.example.bobmukjaku.Model.RetrofitClient
import com.example.bobmukjaku.Model.User
import com.example.bobmukjaku.databinding.ActivityJoinBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class JoinActivity : AppCompatActivity() {
    lateinit var binding: ActivityJoinBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityJoinBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initLayout()
    }

    private fun usernameFromMariaDB(nickname: String, onSuccess: (Boolean) -> Unit) {
        val memberService = RetrofitClient.memberService

        // 서버로부터 모든 회원의 닉네임 정보를 가져오는 API 호출
        val call = memberService.selectAll()

        call.enqueue(object : Callback<List<Member>> {
            override fun onResponse(call: Call<List<Member>>, response: Response<List<Member>>) {
                if (response.isSuccessful) {
                    val memberList = response.body()
                    if (memberList != null) {
                        // 회원 리스트에서 닉네임들을 추출하여 중복 여부를 확인
                        val existingUsernames = memberList.map { it.memberNickName }
                        onSuccess(!existingUsernames.contains(nickname))
                    }
                } else {
                    // 서버로부터 회원 리스트를 받아오지 못했을 때의 처리
                    onSuccess(false)
                }
            }

            override fun onFailure(call: Call<List<Member>>, t: Throwable) {
                // 네트워크 오류 또는 기타 에러가 발생했을 때의 처리
                onSuccess(false)
            }
        })
    }

    private fun initLayout() {
        binding.apply {
            val email = intent.getStringExtra("email") // Join2Activity에서 넘겨준 이메일 주소 가져오기

            // 가입하기 버튼 비활성화
            joinButton.isEnabled=false

            // 받아온 이메일 넣기
            emailArea.setText(email)
            emailArea.isEnabled = false

            // 닉네임 중복확인 버튼
            binding.nickcheckbtn.setOnClickListener {
                val nickname = binding.username.text.toString().trim()

                if (nickname.isEmpty()) {
                    binding.nickConfirm.text = "닉네임을 입력해주세요."
                    binding.nickConfirm.setTextColor(ContextCompat.getColor(applicationContext, R.color.red))
                } else {
                    // 닉네임 중복 확인
                    usernameFromMariaDB(nickname) { isAvailable ->
                        if (isAvailable) {
                            // 중복되지 않은 경우
                            binding.nickConfirm.text = "사용가능한 닉네임입니다."
                            binding.nickConfirm.setTextColor(ContextCompat.getColor(applicationContext, R.color.black))
                        } else {
                            // 중복된 경우
                            binding.nickConfirm.text = "중복된 닉네임입니다."
                            binding.nickConfirm.setTextColor(ContextCompat.getColor(applicationContext, R.color.red))
                        }
                    }
                }
            }

            if (binding.nickConfirm.text == "사용가능한 닉네임입니다.") {
                binding.username.addTextChangedListener {
                    // 사용자가 닉네임을 수정하면 중복확인을 다시 하도록 상태 변경
                    binding.nickConfirm.text = "닉네임 중복확인을 다시 해주세요."
                    binding.nickConfirm.setTextColor(ContextCompat.getColor(applicationContext, R.color.red))
                }
            }

            passwdAreaCheck.addTextChangedListener(object : TextWatcher {
                //입력이 끝났을 때
                // 비밀번호 일치하는지 확인
                override fun afterTextChanged(p0: Editable?) {
                }
                //입력하기 전
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }
                //텍스트 변화가 있을 시
                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    if(passwdArea.text.toString() == passwdAreaCheck.text.toString()){
                        pwConfirm.text = "비밀번호가 일치합니다."

                        // 가입하기 버튼 활성화
                        joinButton.isEnabled=true
                    }
                    else{
                        pwConfirm.text = "비밀번호가 일치하지 않습니다."
                        pwConfirm.setTextColor(ContextCompat.getColor(applicationContext!!, R.color.red))
                    }
                }
            })

            //회원가입 버튼 누를 경우
            joinButton.setOnClickListener {
                //입력한 passwd를 가져와서 변수에 저장
                val passwd = passwdArea.text.toString()

                //인증 데이터베이스(로그인용)에 사용자 추가
                auth = Firebase.auth
                if (email != null) {
                    auth.createUserWithEmailAndPassword(email, passwd)
                        .addOnCompleteListener(this@JoinActivity) { task ->
                            if (task.isSuccessful) {
                                //추가 성공할 경우

                                val uid = FirebaseAuth.getInstance().uid ?: null
                                val username = username.text.toString()
                                val user = User(uid!!, username)

                                //데이터베이스에 회원가입한 사용자 정보 추가
                                val table = Firebase.database.getReference("users")
                                val tuple = table.child(uid)
                                tuple.setValue(user).addOnCompleteListener{
                                    Toast.makeText(this@JoinActivity, "객체넣기 성공", Toast.LENGTH_LONG).show()
                                }
                                    .addOnFailureListener {
                                        Toast.makeText(this@JoinActivity, "객체넣기 실패", Toast.LENGTH_LONG).show()
                                    }

                                //재학생 인증 화면으로 전환
                                val intent = Intent(this@JoinActivity, Join2Activity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.d("LOGIN", "실패")
                            }
                        }
                }
            }
        }
    }
}