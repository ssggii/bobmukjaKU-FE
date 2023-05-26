package com.example.bobmukjaku

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bobmukjaku.Model.User
import com.example.bobmukjaku.databinding.ActivityJoinBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class JoinActivity : AppCompatActivity() {
    lateinit var binding: ActivityJoinBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityJoinBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initLayout()
    }

    private fun initLayout() {
        binding.apply {
            //회원가입 버튼 누를 경우
            joinButton.setOnClickListener {
                //입력한 email, passwd를 가져와서 변수에 저장
                val email = emailArea.text.toString()
                val passwd = passwdArea.text.toString()


                //인증 데이터베이스(로그인용)에 사용자 추가
                auth = Firebase.auth
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

                            //채팅방목록화면으로 전환
                            val intent = Intent(this@JoinActivity, MainActivity::class.java)
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