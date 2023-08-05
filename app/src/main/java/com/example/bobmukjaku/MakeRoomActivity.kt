package com.example.bobmukjaku

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.DatePicker
import android.widget.TextView
import android.widget.TimePicker
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import com.example.bobmukjaku.databinding.ActivityMakeRoomBinding
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class MakeRoomActivity : AppCompatActivity() {

    lateinit var binding: ActivityMakeRoomBinding
//    val uid = intent.getStringExtra("uid") // 사용자 uid 주소 가져오기
    val selectFoodType = "KoreaF"
    val selectPersonType = "P2"

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMakeRoomBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initLayout()
    }

    @RequiresApi(Build.VERSION_CODES.O)
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

        // date_area에 현재 날짜 정보 설정
        val currentDate = getCurrentDate()
        binding.dateArea.text = currentDate

        // date_area 클릭 이벤트 처리
        binding.dateArea.setOnClickListener {
            showDatePicker()
        }

        // 현재 시간 정보 가져오기
        val currentTime = Calendar.getInstance()
        val currentHour = currentTime.get(Calendar.HOUR_OF_DAY)
        val currentMinute = currentTime.get(Calendar.MINUTE)

        // 분을 무조건 0으로 초기화
        val adjustedMinute = 0

        // startTimeArea에 현재 시간 + 1시간 설정
        val startTime = String.format("%02d:%02d", (currentHour + 1) % 24, adjustedMinute)
        binding.startTimeArea.text = startTime

        // endTimeArea에 startTimeArea의 시간 + 2시간 설정
        val endTime = String.format("%02d:%02d", (currentHour + 3) % 24, adjustedMinute)
        binding.endTimeArea.text = endTime

        // time_area 클릭 이벤트 처리
        binding.startTimeArea.setOnClickListener {
            showStartTimePickerDialog()
        }
        binding.endTimeArea.setOnClickListener {
            showEndTimePickerDialog()
        }

        // 완료 버튼 클릭 이벤트 처리
        binding.finishBtn.setOnClickListener {
            // 추후 추가
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showStartTimePickerDialog() {
        showTimePickerDialog { hour, minute ->
            val selectedTime = String.format("%02d:%02d", hour, minute)
            binding.startTimeArea.text = selectedTime
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showEndTimePickerDialog() {
        showTimePickerDialog { hour, minute ->
            val selectedTime = String.format("%02d:%02d", hour, minute)
            binding.endTimeArea.text = selectedTime
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showTimePickerDialog(onTimeSet: (hour: Int, minute: Int) -> Unit) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.timepicker_alert_dialog, null)
        val timePicker = dialogView.findViewById<TimePicker>(R.id.timepicker_alert_two)
        val yesButton = dialogView.findViewById<TextView>(R.id.time_btn_yes)
        val noButton = dialogView.findViewById<TextView>(R.id.time_btn_no)

        // 현재 시간 정보 가져오기
        val currentHour = timePicker.hour
        val currentMinute = timePicker.minute
        Log.i("currentHour", currentHour.toString())

        // 분을 10분 단위로 설정
        val adjustedMinute = (currentMinute / 10) * 10

        // 시간과 분 설정
        timePicker.hour = currentHour
        timePicker.minute = adjustedMinute

        // 분을 10분 단위로 변경하기 위한 리스너 설정
        timePicker.setOnTimeChangedListener { _, hourOfDay, minute ->
            val adjustedMinute = (minute / 10) * 10
            timePicker.minute = adjustedMinute
        }

        val builder = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)

        val alertDialog = builder.create()
        alertDialog.show()

        // 확인 버튼 클릭 이벤트 처리
        yesButton.setOnClickListener {
            val hour = timePicker.hour
            val minute = timePicker.minute

            // 선택한 시간 정보를 콜백 함수를 통해 전달합니다.
            onTimeSet(hour, minute)

            alertDialog.dismiss()
        }

        // 취소 버튼 클릭 이벤트 처리
        noButton.setOnClickListener {
            alertDialog.dismiss()
        }
    }

    // 현재 날짜를 가져오는 함수
    private fun getCurrentDate(): String {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(calendar.time)
    }

    // DatePicker 다이얼로그를 띄우는 함수
    @RequiresApi(Build.VERSION_CODES.O)
    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        // 현재 날짜 기준 2주 뒤까지 날짜 선택 가능
        calendar.add(Calendar.WEEK_OF_YEAR, 2)
        val maxDate = calendar.timeInMillis

        val datePickerDialog = DatePickerDialog(
            this,
            { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
                val selectedDate = formatDate(year, month, dayOfMonth)
                binding.dateArea.text = selectedDate
                LocalDate.parse(selectedDate, DateTimeFormatter.ISO_DATE) // LocalDate로 변환
            },
            year,
            month,
            day
        )

        datePickerDialog.datePicker.minDate = Calendar.getInstance().timeInMillis
        datePickerDialog.datePicker.maxDate = maxDate
        datePickerDialog.show()
    }

    // 날짜를 원하는 포맷으로 변환하는 함수
    private fun formatDate(year: Int, month: Int, dayOfMonth: Int): String {
        val calendar = Calendar.getInstance()
        calendar.set(year, month, dayOfMonth)
        val dateFormat = SimpleDateFormat("yyyy년 M월 d일", Locale.getDefault())
        return dateFormat.format(calendar.time)
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