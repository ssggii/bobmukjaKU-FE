package com.example.bobmukjaku

import android.content.ContentValues.TAG
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.bobmukjaku.databinding.ActivityJoin2Binding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class Join2Activity : AppCompatActivity() {
    lateinit var binding: ActivityJoin2Binding
    private lateinit var auth: FirebaseAuth
    private lateinit var authStateListener: FirebaseAuth.AuthStateListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityJoin2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        initLayout()
        setupAuthStateListener()
    }

    private fun initLayout() {
        val user = Firebase.auth.currentUser

        binding.sendBtn.setOnClickListener {
            if (binding.emailSend.text.contains("인증 메일이 발송되었습니다")) {
                // 인증 완료 여부 확인
                val isEmailVerified = user?.isEmailVerified

                if (isEmailVerified == true) {
                    binding.emailSend.text = "인증이 완료되었습니다"
                } else {
                    binding.emailSend.text = "인증이 완료되지 않았습니다\n추후 추가"
                }
            } else {
                user!!.sendEmailVerification()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d(TAG, "Email 전송")
                            binding.emailSend.text =
                                "인증 메일이 발송되었습니다\n메일함을 확인해 재학생 인증을 완료하고 다시 버튼을 눌러주세요"
                        }
                    }
            }
        }
    }

    private fun setupAuthStateListener() {
        authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser

            // 인증 완료 여부 확인
            val isEmailVerified = user?.isEmailVerified

            if (isEmailVerified == true) {
                binding.emailSend.text = "인증이 완료되었습니다"
            } else {
                binding.emailSend.text = "인증이 완료되지 않았습니다\n추후 추가"
            }
        }
    }

    override fun onStart() {
        super.onStart()
        auth.addAuthStateListener(authStateListener)

        // 인증 완료 여부 초기 확인
        val user = auth.currentUser
        val isEmailVerified = user?.isEmailVerified

        if (isEmailVerified == true) {
            binding.emailSend.text = "인증이 완료되었습니다"
        } else {
            binding.emailSend.text = "인증이 완료되지 않았습니다\n추후 추가"
        }
    }

    override fun onStop() {
        super.onStop()
        auth.removeAuthStateListener(authStateListener)
    }
}