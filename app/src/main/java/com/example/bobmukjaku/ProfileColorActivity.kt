package com.example.bobmukjaku

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.bobmukjaku.databinding.ActivityProfileColorBinding

class ProfileColorActivity : AppCompatActivity() {

    lateinit var binding: ActivityProfileColorBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileColorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
    }

    private fun init() {
        binding.changeButton.setOnClickListener {
            val intent = Intent()
            intent.putExtra("selectedItemId", R.id.forth)
            setResult(RESULT_OK, intent)
            finish()
        }
    }
}