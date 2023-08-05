package com.example.bobmukjaku

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bobmukjaku.Model.Member
import com.example.bobmukjaku.Model.RetrofitClient
import com.example.bobmukjaku.Model.SharedPreferences
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
                verifyPassword(passwd)
            }
        }
    }

    // 사용자 비밀번호 검증 요청
    private fun verifyPassword(passwd: String) {
        val memberService = RetrofitClient.memberService
        val accessToken = SharedPreferences.getString("accessToken", "")

        val authorizationHeader = "Bearer $accessToken"

        val call = accessToken?.let { memberService.selectOne(authorizationHeader) }
        call?.enqueue(object : Callback<Member> {
            override fun onResponse(call: Call<Member>, response: Response<Member>) {
                if (response.isSuccessful) {
                    val member = response.body()
                    val password = member?.memberPassword.toString()
                    if (passwd == password) {
                        Toast.makeText(this@ModifyInfoActivity, "비밀번호 일치", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@ModifyInfoActivity, ModifyInfoActivity2::class.java)
                        startActivity(intent)
                    }else {
                        Toast.makeText(this@ModifyInfoActivity, "비밀번호 불일치", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    val errorCode = response.code()
                    if (errorCode == 400) {
                        // 400 error code (Bad Request)
                        Toast.makeText(
                            this@ModifyInfoActivity,
                            "닉네임을 가져오는데 실패했습니다. 잘못된 요청입니다.",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            this@ModifyInfoActivity,
                            "닉네임을 가져오는데 실패했습니다. 에러 코드: $errorCode",
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
    }
}