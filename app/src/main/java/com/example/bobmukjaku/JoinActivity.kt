package com.example.bobmukjaku

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import com.example.bobmukjaku.Model.Member
import com.example.bobmukjaku.Model.RetrofitClient
import com.example.bobmukjaku.Model.SignUpRequest
import com.example.bobmukjaku.databinding.ActivityJoinBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class JoinActivity : AppCompatActivity() {
    lateinit var binding: ActivityJoinBinding

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
//            emailArea.setText(email)
//            emailArea.isEnabled = false
            emailArea.setText("example@konkuk.ac.kr") // test

            // 닉네임 중복확인 버튼
            binding.nickcheckbtn.setOnClickListener {
                val nickname = binding.username.text.toString().trim()

                if (nickname.length < 2) {
                    binding.nickConfirm.text = "닉네임은 최소 2자 이상 입력해주세요."
                    binding.nickConfirm.setTextColor(ContextCompat.getColor(applicationContext, R.color.red))
                } else {
                    // 닉네임 중복 확인
                    usernameFromMariaDB(nickname) { isAvailable ->
                        if (!isAvailable) {
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

            binding.username.addTextChangedListener {
                if (binding.nickConfirm.text == "사용가능한 닉네임입니다.") {
                    // 사용자가 닉네임을 수정하면 중복확인을 다시 하도록 상태 변경
                    binding.nickConfirm.text = "닉네임 중복확인을 다시 해주세요."
                    binding.nickConfirm.setTextColor(ContextCompat.getColor(applicationContext, R.color.red))
                }
            }

            // 비밀번호 조건
            val passwordPattern = "^(?=.*[!@^&,.?])(?=.*[A-Za-z]).{8,15}$".toRegex()

            passwdArea.addTextChangedListener(object : TextWatcher {
                //입력이 끝났을 때
                override fun afterTextChanged(editable: Editable?) {
                }
                //입력하기 전
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                }
                //텍스트 변화가 있을 시
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    val passwd = passwdArea.text.toString()

                    // 비밀번호 조건 검사
                    if (passwd.length in 8..15 && passwd.matches(passwordPattern)) {
                        pwInfo.text = "해당 비밀번호는 사용 가능합니다."
                        pwInfo.setTextColor(ContextCompat.getColor(applicationContext, R.color.black))
                    } else {
                        pwInfo.text = "비밀번호는 8~15자이며, 다음 특수문자('!', '@', '^', '&', ',', '.', '?') 중 하나 이상을 포함시켜야 합니다.)"
                        pwInfo.setTextColor(ContextCompat.getColor(applicationContext, R.color.red))
                        if (pwConfirm.text == "비밀번호가 일치합니다.") {
                            pwConfirm.text = ""
                            // 가입하기 버튼 비활성화
                            joinButton.isEnabled = false
                        }
                    }
                }
            })

            passwdAreaCheck.addTextChangedListener(object : TextWatcher {
                //입력이 끝났을 때
                override fun afterTextChanged(editable: Editable?) {
                }
                //입력하기 전
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                }
                //텍스트 변화가 있을 시
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    val passwdCheck = passwdAreaCheck.text.toString()
                    val passwd = passwdArea.text.toString()

                    if (passwdCheck == passwd) {
                        pwConfirm.text = "비밀번호가 일치합니다."
                        pwConfirm.setTextColor(ContextCompat.getColor(applicationContext, R.color.black))

                        // 가입하기 버튼 활성화
                        joinButton.isEnabled = true
                    } else {
                        pwConfirm.text = "비밀번호가 일치하지 않습니다."
                        pwConfirm.setTextColor(ContextCompat.getColor(applicationContext, R.color.red))

                        // 가입하기 버튼 비활성화
                        joinButton.isEnabled = false
                    }
                }
            })

            //회원가입 버튼 누를 경우
            joinButton.setOnClickListener {
                if (binding.nickConfirm.text == "사용가능한 닉네임입니다.") {
                    if (pwInfo.text == "해당 비밀번호는 사용 가능합니다.") {
                        if (pwConfirm.text == "비밀번호가 일치합니다.") {
                            //입력한 닉네임, passwd를 가져와서 변수에 저장
                            val email = emailArea.text.toString()
                            val nickname = username.text.toString()
                            val passwd = passwdArea.text.toString()

                            val signUpRequest = SignUpRequest(memberEmail = email, password = passwd, nickname = nickname)

                            val call = RetrofitClient.memberService.insertMember(signUpRequest)

//                            // 사용자 정보를 담는 Member 객체 생성
//                            val member = Member(memberEmail = email, memberNickName = nickname, memberPassword = passwd, certificatedAt = "", profileColor = "bg1", rate = 45, uid=0)
//
//                            // MemberService의 insertMember 메서드를 호출하여 사용자 정보를 DB에 저장
//                            val call = RetrofitClient.memberService.insertMember(member)

                            // 네트워크 요청을 비동기적으로 실행하도록 호출
                            call.enqueue(object : Callback<Void> {
                                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                                    if (response.isSuccessful) {
                                        // 데이터 삽입 성공
                                        Toast.makeText(this@JoinActivity, "회원가입이 완료되었습니다.", Toast.LENGTH_SHORT).show()

                                        val intent = Intent(this@JoinActivity, LoginActivity::class.java)
                                        intent.putExtra("email", email)
                                        startActivity(intent)
                                    } else if (response.code() == 400) { // 데이터 삽입 실패
                                        // 400 error code (Bad Request)
                                        Toast.makeText(this@JoinActivity, "회원가입에 실패했습니다. 잘못된 요청입니다.", Toast.LENGTH_SHORT).show()
                                    } else {
                                        // 400 error code 아닐 때
                                        Toast.makeText(this@JoinActivity, "회원가입에 실패했습니다.", Toast.LENGTH_SHORT).show()
                                    }
                                }

                                override fun onFailure(call: Call<Void>, t: Throwable) {
                                    // 네트워크 오류 처리
                                    Toast.makeText(this@JoinActivity, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                                }
                            })
                        } else {
                            Toast.makeText(this@JoinActivity, "비밀번호 확인을 다시 해주세요.", Toast.LENGTH_SHORT)
                        }
                    } else {
                        Toast.makeText(this@JoinActivity, "비밀번호를 다시 입력해주세요.", Toast.LENGTH_SHORT)
                    }
                } else {
                    Toast.makeText(this@JoinActivity, "닉네임을 다시 입력해주세요.", Toast.LENGTH_SHORT)
                }
            }
        }
    }
}