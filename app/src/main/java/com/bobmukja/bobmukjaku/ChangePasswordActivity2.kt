package com.bobmukja.bobmukjaku

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bobmukja.bobmukjaku.Dto.UpdatePasswordDto
import com.bobmukja.bobmukjaku.Model.RetrofitClient
import com.bobmukja.bobmukjaku.MyApp.MyApp
import com.bobmukja.bobmukjaku.databinding.ActivityChangePassword2Binding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChangePasswordActivity2 : AppCompatActivity() {
    lateinit var binding: ActivityChangePassword2Binding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangePassword2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        MyApp.setAppContext(this)
        initLayout()
    }

    private fun initLayout() {
        binding.apply {
            val getEmail = intent.getStringExtra("email") // ChangePasswordActivity에서 넘겨준 이메일 주소 가져오기

            email.text = getEmail

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
                            modifyButton.isEnabled = false
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
                        modifyButton.isEnabled = true
                    } else {
                        pwConfirm.text = "비밀번호가 일치하지 않습니다."
                        pwConfirm.setTextColor(ContextCompat.getColor(applicationContext, R.color.red))

                        // 가입하기 버튼 비활성화
                        modifyButton.isEnabled = false
                    }
                }
            })

            modifyButton.setOnClickListener {
                if (pwInfo.text == "해당 비밀번호는 사용 가능합니다.") {
                    if (pwConfirm.text == "비밀번호가 일치합니다.") {
                        //입력한 닉네임, passwd를 가져와서 변수에 저장
                        val email = intent.getStringExtra("email")
                        val passwd = passwdArea.text.toString()

                        // 네트워크 요청을 비동기적으로 실행하도록 호출
                        RetrofitClient.memberService.updateMemberWithoutLogin(UpdatePasswordDto(passwd, email))
                            .enqueue(object : Callback<Void>{
                                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                                    Log.i("rrr", response.code().toString())
                                    when(response.code()){
                                        200->{
                                            Log.i("updatePassword", "success")
                                            val intent = Intent(this@ChangePasswordActivity2, LoginActivity::class.java)
                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                                            startActivity(intent)
                                        }
                                        else->{
                                            //Log.i("updatePassword", response.code().toString())
                                            Toast.makeText(this@ChangePasswordActivity2, "이메일과 일치하는 계정정보가 없습니다.", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }

                                override fun onFailure(call: Call<Void>, t: Throwable) {
                                    Toast.makeText(this@ChangePasswordActivity2, "네트워크 에러.", Toast.LENGTH_SHORT).show()
                                }

                            })


                    } else {
                        Toast.makeText(this@ChangePasswordActivity2, "비밀번호 확인을 다시 해주세요.", Toast.LENGTH_SHORT)
                    }
                } else {
                    Toast.makeText(this@ChangePasswordActivity2, "비밀번호를 다시 입력해주세요.", Toast.LENGTH_SHORT)
                }
            }


        }
    }
}