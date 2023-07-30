package com.example.bobmukjaku

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.bobmukjaku.databinding.ActivityMakeRoomBinding

class MakeRoomActivity : AppCompatActivity() {

    lateinit var binding: ActivityMakeRoomBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMakeRoomBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initLayout()
    }

    private fun initLayout() {
        // 모집방 개설 닫기 클릭 이벤트 처리
        binding.cancelBtn.setOnClickListener {
            val intent = Intent(this@MakeRoomActivity, MainActivity::class.java)
            startActivity(intent)
        }
    }
}