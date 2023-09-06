package com.example.bobmukjaku

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.graphics.PointF
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.widget.DatePicker
import android.widget.TextView
import android.widget.TimePicker
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.bobmukjaku.databinding.ActivityBobAppointmentBinding
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.NaverMap
import com.naver.maps.map.overlay.Marker
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class BobAppointmentActivity : AppCompatActivity() {
    lateinit var binding: ActivityBobAppointmentBinding
    private lateinit var viewModel: MapListViewModel

    override fun onAttachFragment(childFragment: Fragment) {
        super.onAttachFragment(childFragment)
        attachContextToViewModel(childFragment.requireContext())
    }

    private fun attachContextToViewModel(context: Context) {
        val repository = RestaurantRepository()
        val viewModelFactory = MapListViewModelFactory(repository)
        viewModel = ViewModelProvider(this, viewModelFactory)[MapListViewModel::class.java]
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBobAppointmentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initLayout()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initLayout() {
        binding.apply {
            dateTime.setOnClickListener {
                showStartTimePickerDialog()
            }

            setCompleteBtn.setOnClickListener{

            }

            editText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                    // 사용하지 않음
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    lifecycleScope.launch {
                        val dong = listOf(
                            "11215710",
                            "30110590",
                            "11215850",
                            "11215860",
                            "11215870",
                            "41390581"
                        )
                        val indsMclsCdList =
                            listOf("I201", "I202", "I203", "I204", "I205", "I206", "I211")

                        for (lists in indsMclsCdList) {
                            viewModel.fetchRestaurantList(lists)
                            val restaurantList = viewModel.restaurantList.value ?: emptyList()

                            for (restaurant in restaurantList) {
                                if (restaurant.bizesNm.contains(s.toString())) {

                                }
                            }
                        }
                    }
                }

                override fun afterTextChanged(s: Editable?) {
                    // 사용하지 않음
                }
            })
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun showStartTimePickerDialog() {
        showTimePickerDialog { hour, minute ->
            val selectedTime = String.format("%02d:%02d", hour, minute)
            //binding.startTimeArea.text = selectedTime
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showEndTimePickerDialog() {
        showTimePickerDialog { hour, minute ->
            val selectedTime = String.format("%02d:%02d", hour, minute)
            //binding.endTimeArea.text = selectedTime
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
                //binding.dateArea.text = selectedDate
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
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(calendar.time)
    }
}