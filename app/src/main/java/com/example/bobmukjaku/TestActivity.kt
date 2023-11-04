package com.example.bobmukjaku

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.bobmukjaku.databinding.ActivityTestBinding

class TestActivity : AppCompatActivity() {

    lateinit var binding: ActivityTestBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initLayout()
    }

    private fun initLayout() {
        binding.button.setOnClickListener {
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("placeName", "음식점이름1")
            intent.putExtra("placeAddress", "음식점이름2")
            intent.putExtra("imageUrl", "/MA010120220804929030/2_bbb_1696237470695.jpg")
            setResult(RESULT_OK, intent)
            finish()
        }
    }
}