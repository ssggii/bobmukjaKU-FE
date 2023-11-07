package com.bobmukja.bobmukjaku

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bobmukja.bobmukjaku.CustomTimePicker.Companion.getDisplayedMinute
import com.bobmukja.bobmukjaku.CustomTimePicker.Companion.setTimeInterval
import com.bobmukja.bobmukjaku.Dto.NoticeDto
import com.bobmukja.bobmukjaku.Model.RestaurantList
import com.bobmukja.bobmukjaku.databinding.ActivityBobAppointmentBinding
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class BobAppointmentActivity : AppCompatActivity() {
    lateinit var binding: ActivityBobAppointmentBinding
    private lateinit var viewModel: MapListViewModel

    private val roomId : Long by lazy {
        intent.getLongExtra("roomId", -1)
    }


    private var restaurants = listOf<RestaurantList>()

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

        attachContextToViewModel(this)
        initRestaurantList()
        initLayout()
    }


    //api로 모든 음식점 리스트를 불러와 restaurants배열에 저장 후, autoCompleteTextView를 초기화하는 메서드
    private fun initRestaurantList(){
        lifecycleScope.launch {
            val indsMclsCdList =
                listOf("I201", "I202", "I203", "I204", "I205", "I206", "I211")

            for (lists in indsMclsCdList) {
                viewModel.fetchRestaurantList(lists)
                val restaurantList = viewModel.restaurantList.value ?: emptyList()
                restaurants = restaurants + restaurantList
            }
            Log.i("kkk", restaurants.toString())

            //autoCompleteTextView를 초기화
            initAutoCompleteTextView()
        }
    }

    val test = arrayListOf<String>()
    //AutoCompleteTextView 초기화
    private fun initAutoCompleteTextView() {

        //val adapter = ArrayAdapter(this@BobAppointmentActivity, android.R.layout.simple_spinner_dropdown_item, restaurants)
        val adapter = ArrayAdapter(this@BobAppointmentActivity, android.R.layout.simple_spinner_dropdown_item, restaurants)
        binding.apply {
           autocompleteRestaurant.setAdapter(adapter)
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun initLayout() {
        binding.apply {
           // val startTimeInNotice = intent.getStringExtra("noticeStartTime")
            Firebase.database.getReference("chatRoom/$roomId/notice/starttime").get().addOnCompleteListener {
                if(it.isSuccessful){
                    var startTime = ""
                    var result = it.result.value.toString()
                    if(result == "null"){
                        meetingdate.text = intent.getStringExtra("meetingDate")
                        val data = intent.getStringExtra("starttime")?:"00:00:00"
                        val hour = data.substring(0,2)
                        val minute = data.substring(3,5)
                        val timeFormat = "%02d:%02d"
                        if(hour.toInt() > 11){
                            startTime += "오후 "
                            startTime += timeFormat.format(hour.toInt() - 12, minute.toInt())
                        }else{
                            startTime += "오전 "
                            startTime += timeFormat.format(hour.toInt(), minute.toInt())
                        }
                    }else{
                        startTime = result
                    }
                    dateTime.text = startTime
                }
            }


            dateTime.setOnClickListener {
                showStartTimePickerDialog()
            }

            cancelBtn.setOnClickListener {
                val intent = Intent(this@BobAppointmentActivity, ChatActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                startActivity(intent)
            }


            setCompleteBtn.setOnClickListener{
                val selectedRestaurant = restaurants.find { it.bizesNm==autocompleteRestaurant.text.toString()}

                if (selectedRestaurant != null) {//실제 음식점 이름을 입력했을 경우에만 공지등록
                    val restaurantId = selectedRestaurant.bizesId
                    val restaurantName = selectedRestaurant.bizesNm
                    val newStartTime = binding.dateTime.text.toString()

                    //파이어베이스 realtimebase에 공지추가
                    Log.i("kim", "$roomId|$restaurantId|$restaurantName")
                    val rf = Firebase.database.getReference("chatRoom/$roomId/notice")
                    rf.setValue(NoticeDto(restaurantId,restaurantName, newStartTime)).addOnCompleteListener {
                        if(it.isSuccessful){
                            Toast.makeText(this@BobAppointmentActivity, "밥공지가 등록되었습니다.", Toast.LENGTH_SHORT).show()

                            val intent = Intent(this@BobAppointmentActivity, ChatActivity::class.java)
                            //공지사항에서 채팅방목록으로 전환했을때, 뒤로가기를 누르면 다시 공지화면으로 가는 것을 방지하기 위하여 flag설정
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                            intent.putExtra("place", restaurantName)
                            startActivity(intent)
                        }else{
                            Toast.makeText(this@BobAppointmentActivity, "밥공지가 등록에 실패했습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }else{//실제 음식점이름을 입력한 것이 아니라면 공지등록 실패
                    Toast.makeText(this@BobAppointmentActivity, "공지를 등록하려면 실제 음식점명을 입력해야합니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun showStartTimePickerDialog() {
        showTimePickerDialog { hour, minute ->
            //var selectedTime = String.format("%02d:%02d", hour, minute)
            //var hour = selectedTime.substring(0,2).toInt()
            //val minute = selectedTime.substring(3,5).toInt()

            Log.i("starttime", "$hour/$minute")
            val timeFormat = "%02d:%02d"
            var dateTime = ""

            if(hour == 0){
                dateTime += "오전 "
                dateTime += timeFormat.format(12, minute)
            }else if(hour == 12){
                dateTime += "오후 "
                dateTime += timeFormat.format(12, minute)
            }
            else if(hour > 11){
                dateTime += "오후 "
                dateTime += timeFormat.format(hour - 12, minute)
            }else{
                dateTime += "오전 "
                dateTime += timeFormat.format(hour, minute)
            }
            binding.dateTime.text = dateTime
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showTimePickerDialog(onTimeSet: (hour: Int, minute: Int) -> Unit) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.timepicker_alert_dialog, null)
        val timePicker = dialogView.findViewById<TimePicker>(R.id.timepicker_alert_two)
        val yesButton = dialogView.findViewById<TextView>(R.id.time_btn_yes)
        val noButton = dialogView.findViewById<TextView>(R.id.time_btn_no)


        timePicker.setTimeInterval(10)

        // 현재 시간 정보 가져오기
        val currentHour = timePicker.hour
        val currentMinute = timePicker.getDisplayedMinute()



        val builder = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)

        val alertDialog = builder.create()
        alertDialog.show()


        // 확인 버튼 클릭 이벤트 처리
        yesButton.setOnClickListener {
            val hour = timePicker.hour
            //val minute = timePicker.minute
            val minute = timePicker.getDisplayedMinute()

            Log.i("currentHour", "$hour : $minute")
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