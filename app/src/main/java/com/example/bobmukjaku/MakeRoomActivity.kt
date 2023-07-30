package com.example.bobmukjaku

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
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
        // 음식종류 각 버튼에 클릭 이벤트 설정
        binding.KoreaF.setOnClickListener { selectFoodType(binding.KoreaF) }
        binding.JapanF.setOnClickListener { selectFoodType(binding.JapanF) }
        binding.ForeignF.setOnClickListener { selectFoodType(binding.ForeignF) }
        binding.ChinaF.setOnClickListener { selectFoodType(binding.ChinaF) }
        binding.ectF.setOnClickListener { selectFoodType(binding.ectF) }

        // 인원수 각 버튼에 클릭 이벤트 설정
        binding.P2.setOnClickListener { selectPersonType(binding.P2) }
        binding.P3.setOnClickListener { selectPersonType(binding.P3) }
        binding.P4.setOnClickListener { selectPersonType(binding.P4) }
        binding.P5.setOnClickListener { selectPersonType(binding.P5) }
        binding.P6.setOnClickListener { selectPersonType(binding.P6) }

        // 모집방 개설 닫기 클릭 이벤트 처리
        binding.cancelBtn.setOnClickListener {
            val intent = Intent(this@MakeRoomActivity, MainActivity::class.java)
            startActivity(intent)
        }
    }

    // 음식 종류 버튼 클릭 시 호출되는 함수
    private fun selectFoodType(selectedButton: AppCompatButton) {
        // 모든 버튼의 배경색과 텍스트 색상 초기화
        binding.KoreaF.backgroundTintList = getColorStateList(R.color.gray)
        binding.KoreaF.setTextColor(ContextCompat.getColor(this, R.color.black))
        binding.JapanF.backgroundTintList = getColorStateList(R.color.gray)
        binding.JapanF.setTextColor(ContextCompat.getColor(this, R.color.black))
        binding.ForeignF.backgroundTintList = getColorStateList(R.color.gray)
        binding.ForeignF.setTextColor(ContextCompat.getColor(this, R.color.black))
        binding.ChinaF.backgroundTintList = getColorStateList(R.color.gray)
        binding.ChinaF.setTextColor(ContextCompat.getColor(this, R.color.black))
        binding.ectF.backgroundTintList = getColorStateList(R.color.gray)
        binding.ectF.setTextColor(ContextCompat.getColor(this, R.color.black))

        // 선택한 버튼의 스타일 변경
        selectedButton.backgroundTintList = getColorStateList(R.color.main)
        selectedButton.setTextColor(ContextCompat.getColor(this, R.color.white))
    }

    // 인원수 버튼 클릭 시 호출되는 함수
    private fun selectPersonType(selectedButton: AppCompatButton) {
        // 모든 버튼의 배경색과 텍스트 색상 초기화
        binding.P2.backgroundTintList = getColorStateList(R.color.gray)
        binding.P2.setTextColor(ContextCompat.getColor(this, R.color.black))
        binding.P3.backgroundTintList = getColorStateList(R.color.gray)
        binding.P3.setTextColor(ContextCompat.getColor(this, R.color.black))
        binding.P4.backgroundTintList = getColorStateList(R.color.gray)
        binding.P4.setTextColor(ContextCompat.getColor(this, R.color.black))
        binding.P5.backgroundTintList = getColorStateList(R.color.gray)
        binding.P5.setTextColor(ContextCompat.getColor(this, R.color.black))
        binding.P6.backgroundTintList = getColorStateList(R.color.gray)
        binding.P6.setTextColor(ContextCompat.getColor(this, R.color.black))

        // 선택한 버튼의 스타일 변경
        selectedButton.backgroundTintList = getColorStateList(R.color.main)
        selectedButton.setTextColor(ContextCompat.getColor(this, R.color.white))
    }
}