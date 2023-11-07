package com.bobmukja.bobmukjaku

import android.app.Activity
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bobmukja.bobmukjaku.databinding.ActivityTestBinding

class TestActivity : AppCompatActivity() {

    lateinit var binding: ActivityTestBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initLayout()
    }

    private fun initLayout() {
        binding.apply {
            button.setOnClickListener {
                intent.putExtra("placeName", "건대 맛집")
                intent.putExtra("placeAddress", "광진구")
                intent.putExtra("imageUrl", "/MA010120220803043554/1_test_1698055424155.jpg")
                setResult(Activity.RESULT_OK, intent)
                finish()
            }
        }
    }
}