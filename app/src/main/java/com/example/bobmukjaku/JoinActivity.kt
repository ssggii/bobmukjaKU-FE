package com.example.bobmukjaku

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.bobmukjaku.Model.User
import com.example.bobmukjaku.databinding.ActivityJoinBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class JoinActivity : AppCompatActivity() {
    lateinit var binding: ActivityJoinBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityJoinBinding.inflate(layoutInflater)
        setContentView(binding.root)

        usernameFromFirebase()
        initLayout()
    }

    private fun usernameFromFirebase() {
        val db = Firebase.database.getReference("users")
        binding.nickcheckbtn.setOnClickListener {
            db.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    binding.nickConfirm.text = "사용가능한 닉네임입니다."
                    binding.nickConfirm.setTextColor(ContextCompat.getColor(applicationContext!!, R.color.black))

                    if (binding.username.text.toString().isEmpty()) {
                        binding.nickConfirm.text = "닉네임을 입력해주세요."
                        binding.nickConfirm.setTextColor(ContextCompat.getColor(applicationContext!!, R.color.red))
                    }

                    for (users in dataSnapshot.children) {
                        val name = users.child("username").value.toString()
                        Log.i("user", name)

                        if (binding.username.text.toString() == name) {
                            binding.nickConfirm.text = "중복된 닉네임입니다."
                            binding.nickConfirm.setTextColor(ContextCompat.getColor(applicationContext!!, R.color.red))
                            break
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Error handling
                }
            })
        }
    }

    private fun initLayout() {
        binding.apply {
            // 가입하기 버튼 비활성화
            joinButton.isEnabled=false

            emailArea.addTextChangedListener(object : TextWatcher {
                //입력이 끝났을 때
                // 학교 이메일 주소인지 확인
                override fun afterTextChanged(p0: Editable?) {
                    if(!emailArea.text.toString().contains("konkuk.ac.kr")){
                        emailConfirm.text = "건국대학교 이메일 주소를 입력해주세요."
                        emailConfirm.setTextColor(ContextCompat.getColor(applicationContext!!, R.color.red))
                    }
                    else{
                        emailConfirm.text = ""
                    }
                }
                //입력하기 전
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }
                //텍스트 변화가 있을 시
                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    if(!emailArea.text.toString().contains("konkuk.ac.kr")){
                        emailConfirm.text = "건국대학교 이메일 주소를 입력해주세요."
                    }
                    else{
                        emailConfirm.text = ""
                    }
                }
            })

            passwdAreaCheck.addTextChangedListener(object : TextWatcher {
                //입력이 끝났을 때
                // 비밀번호 일치하는지 확인
                override fun afterTextChanged(p0: Editable?) {
//                    if(passwdArea.text.toString().equals(passwdAreaCheck.text.toString())){
//                        pwConfirm.text = "비밀번호가 일치합니다."
//
//                        // 가입하기 버튼 활성화
//                        joinButton.isEnabled=true
//                    }
//                    else{
//                        pwConfirm.text = "비밀번호가 일치하지 않습니다."
//                        pwConfirm.setTextColor(ContextCompat.getColor(applicationContext!!, R.color.red))
//                    }
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