package com.example.bobmukjaku

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bobmukjaku.Model.Member
import com.example.bobmukjaku.Model.RetrofitClient
import com.example.bobmukjaku.databinding.ActivityModifyInfoBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ModifyInfoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityModifyInfoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityModifyInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 확인 버튼 클릭 시 비밀번호 검증
        binding.okayButton.setOnClickListener {
            val passwd = binding.passwdArea.text.toString()
            if (passwd.isEmpty()) {
                Toast.makeText(this, "비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
            } else {
                // 사용자의 uid 가져오기
                val uid = getLoggedInUserId() // SharedPreferences에서 uid 값을 읽어오는 함수

                if (uid != -1L) {
                    // 서버에 사용자 비밀번호 검증 요청
                    verifyPassword(uid, passwd)
                } else {
                    // 로그인 정보가 없거나 uid 값을 가져오지 못한 경우
                    Toast.makeText(this, "로그인 정보를 가져오는데 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // 사용자 비밀번호 검증 요청
    private fun verifyPassword(uid: Long, password: String) {
        val request = RetrofitClient.memberService.selectOne(uid)
        CoroutineScope(Dispatchers.IO).launch {
            request.enqueue(object : Callback<Member> {
                override fun onResponse(call: Call<Member>, response: Response<Member>) {
                    if (response.isSuccessful) {
                        val member = response.body()
                        if (member != null) {
                            val serverPassword = member.memberPassword
                            if (serverPassword == password) {
                                // 서버에서 받아온 비밀번호와 사용자가 입력한 비밀번호가 일치하는 경우
                                // 비밀번호 검증 성공 처리
                                val intent = Intent(this@ModifyInfoActivity, ModifyInfoActivity2::class.java)
                                startActivity(intent) // 추후 수정 (PROFILE_MODIFY_REQUEST_CODE = 101 받아서 넘겨 줘야 함?)
                            } else {
                                // 서버에서 받아온 비밀번호와 사용자가 입력한 비밀번호가 일치하지 않는 경우
                                // 비밀번호 검증 실패 처리
                                Toast.makeText(this@ModifyInfoActivity, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            // 서버에서 사용자 정보를 가져오지 못한 경우
                            Toast.makeText(this@ModifyInfoActivity, "사용자 정보를 가져오는데 실패했습니다.", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        // 서버 응답에 실패한 경우
                        Toast.makeText(this@ModifyInfoActivity, "서버 응답에 실패했습니다.", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Member>, t: Throwable) {
                    // 네트워크 오류 등 요청 실패 처리
                    Toast.makeText(this@ModifyInfoActivity, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    // SharedPreferences에서 사용자의 uid 값을 가져오는 함수
    // 이 함수는 사용자가 로그인한 상태에서만 호출되어야 하며, 로그인하지 않은 경우 -1을 반환
    private fun getLoggedInUserId(): Long {
        // TODO: SharedPreferences에서 uid 값을 읽어와서 반환하는 로직을 구현해야 합니다.
        // val sharedPreference = applicationContext.getSharedPreferences("your_preference_key", Context.MODE_PRIVATE)
        // return sharedPreference.getLong("uid", -1L)
        return -1L
    }
}