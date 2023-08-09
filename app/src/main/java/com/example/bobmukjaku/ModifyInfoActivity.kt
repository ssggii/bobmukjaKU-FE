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
import com.example.bobmukjaku.Model.*
import com.example.bobmukjaku.databinding.ActivityModifyInfoBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDate

class ModifyInfoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityModifyInfoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityModifyInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 닉네임 중복 검사
        binding.nickcheckbtn.setOnClickListener {
            val nickname = binding.username.text.toString().trim()

            if (nickname.length < 2) {
                binding.nickConfirm.text = "닉네임은 최소 2자 이상 입력해주세요."
                binding.nickConfirm.setTextColor(ContextCompat.getColor(applicationContext, R.color.red))
            } else {
                // 닉네임 중복 확인
                usernameFromDB(nickname) { isAvailable ->
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
                binding.nickConfirm.text = "닉네임 변경을 원하면 중복확인을 다시 해주세요."
                binding.nickConfirm.setTextColor(ContextCompat.getColor(applicationContext, R.color.red))
            }
        }

        // 비밀번호 조건 & 일치 여부
        checkedPassword()

        // 확인 버튼 클릭 시 비밀번호 검증
        binding.modifyButton.setOnClickListener {
            val nick = binding.username.text.toString()
            val passwd = binding.passwdArea.text.toString()
            if (nick.isEmpty() && passwd.isEmpty()) {
                Toast.makeText(
                    this@ModifyInfoActivity,
                    "변경된 사항이 없습니다.",
                    Toast.LENGTH_SHORT
                ).show()
            } else if (nick.isEmpty()) {
                updatePassword(passwd)
                Toast.makeText(
                    this@ModifyInfoActivity,
                    "비밀번호가 업데이트되었습니다.",
                    Toast.LENGTH_SHORT
                ).show()
            } else if (passwd.isEmpty()) {
                updateNickname(nick)
                Toast.makeText(
                    this@ModifyInfoActivity,
                    "닉네임이 업데이트되었습니다.",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                updateNickname(nick)
                updatePassword(passwd)
                Toast.makeText(
                    this@ModifyInfoActivity,
                    "닉네임과 비밀번호가 업데이트되었습니다.",
                    Toast.LENGTH_SHORT
                ).show()
            }

            val intent = Intent()
            intent.putExtra("selectedItemId", R.id.forth)
            setResult(RESULT_OK, intent)
            finish()
        }
    }

    private fun checkedPassword() {
        binding.apply {
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
                    } else {
                        pwConfirm.text = "비밀번호가 일치하지 않습니다."
                        pwConfirm.setTextColor(ContextCompat.getColor(applicationContext, R.color.red))
                    }
                }
            })
        }
    }

    private fun usernameFromDB(nickname: String, onSuccess: (Boolean) -> Unit) {
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
                        onSuccess(existingUsernames.contains(nickname))
                    }
                } else {
                    // 서버로부터 회원 리스트를 받아오지 못했을 때의 처리
                    Log.i("회원가입[닉네임중복확인]: ", "서버로부터 리스트 받지 못함")
                    onSuccess(false)
                }
            }

            override fun onFailure(call: Call<List<Member>>, t: Throwable) {
                // 네트워크 오류 또는 기타 에러가 발생했을 때의 처리
                Log.i("회원가입[닉네임중복확인]: ", "네트워크 오류 및 기타 에러")
                onSuccess(false)
            }
        })
    }

    private fun updateNickname(nick: String) {
        val memberService = RetrofitClient.memberService
        val accessToken = SharedPreferences.getString("accessToken", "")

        val authorizationHeader = "Bearer $accessToken"

        val requestBody = mapOf("memberNickName" to nick)

        val call = accessToken?.let {
            memberService.updateMember(
                authorizationHeader,
                requestBody
            )
        }
        call?.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    // 성공적으로 업데이트됨
                    Toast.makeText(
                        this@ModifyInfoActivity,
                        "닉네임이 업데이트되었습니다.",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    val errorCode = response.code()
                    Toast.makeText(
                        this@ModifyInfoActivity,
                        "닉네임 업데이트 실패. 에러 코드: $errorCode",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                // 네트워크 오류 처리
                Toast.makeText(this@ModifyInfoActivity, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updatePassword(passwd: String) {
        val memberService = RetrofitClient.memberService
        val accessToken = SharedPreferences.getString("accessToken", "")

        val authorizationHeader = "Bearer $accessToken"
        var checkedPasswd = ""

        val call = accessToken?.let { memberService.selectOne(authorizationHeader) }
        call?.enqueue(object : Callback<Member> {
            override fun onResponse(call: Call<Member>, response: Response<Member>) {
                if (response.isSuccessful) {
                    val member = response.body()
                    checkedPasswd = member?.memberPassword.toString()
                } else {
                    val errorCode = response.code()
                    if (errorCode == 400) {
                        // 400 error code (Bad Request)
                        Toast.makeText(
                            this@ModifyInfoActivity,
                            "비밀번호를 가져오는데 실패했습니다. 잘못된 요청입니다.",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            this@ModifyInfoActivity,
                            "비밀번호를 가져오는데 실패했습니다. 에러 코드: $errorCode",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

            override fun onFailure(call: Call<Member>, t: Throwable) {
                // 네트워크 오류 처리
                Toast.makeText(this@ModifyInfoActivity, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
            }
        })

        val passwdRequest = UpdatePassword(tobePassword = passwd, checkPassword = checkedPasswd)

        val call2 = accessToken?.let {
            memberService.updatePassword(
                authorizationHeader,
                passwdRequest
            )
        }
        call2?.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    // 성공적으로 업데이트됨
                    Toast.makeText(
                        this@ModifyInfoActivity,
                        "비밀번호가 업데이트되었습니다.",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    val errorCode = response.code()
                    Toast.makeText(
                        this@ModifyInfoActivity,
                        "비밀번호가 업데이트 실패. 에러 코드: $errorCode",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                // 네트워크 오류 처리
                Toast.makeText(this@ModifyInfoActivity, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
            }
        })
    }
}