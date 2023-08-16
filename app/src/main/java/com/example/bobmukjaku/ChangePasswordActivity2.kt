package com.example.bobmukjaku

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.bobmukjaku.databinding.ActivityChangePassword2Binding

class ChangePasswordActivity2 : AppCompatActivity() {
    lateinit var binding: ActivityChangePassword2Binding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangePassword2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        initLayout()
    }

    private fun initLayout() {
        binding.apply {
//            val getEmail = intent.getStringExtra("email") // ChangePasswordActivity에서 넘겨준 이메일 주소 가져오기
//
//            email.text = getEmail
        }
    }
}