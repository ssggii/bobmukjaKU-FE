package com.example.bobmukjaku

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.DatePicker
import android.widget.TextView
import android.widget.TimePicker
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import com.example.bobmukjaku.Model.*
import com.example.bobmukjaku.databinding.ActivityMakeRoomBinding
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class MakeRoomActivity : AppCompatActivity() {

    lateinit var binding: ActivityMakeRoomBinding

    private var selectFoodType = "한식"
    private var selectPersonType = 2

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
        binding.P2.setOnClickListener { selectPersonType(2) }
        binding.P3.setOnClickListener { selectPersonType(3) }
        binding.P4.setOnClickListener { selectPersonType(4) }
        binding.P5.setOnClickListener { selectPersonType(5) }
        binding.P6.setOnClickListener { selectPersonType(6) }

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
            // 모집방 정보 db에 저장
            val accessToken = SharedPreferences.getString("accessToken", "")
            val authorizationHeader = "Bearer $accessToken"

            val insertRequest = InsertChatRoomRequest(
                roomName = binding.nameArea.text.toString(),
                date = binding.dateArea.text.toString(),
                startTime = binding.startTimeArea.text.toString(),
                endTime = binding.endTimeArea.text.toString(),
                kindOfFood = selectFoodType,
                total = selectPersonType
            )

            val call = RetrofitClient.chatRoomService.insertChatRoom(
                authorizationHeader,
                insertRequest
            )

            // 네트워크 요청을 비동기적으로 실행하도록 호출
            call.enqueue(object : Callback<ChatRoom> {
                override fun onResponse(call: Call<ChatRoom>, response: Response<ChatRoom>) {
                    if (response.isSuccessful) {
                        val insertedChatRoom = response.body()
                        Log.i("success", insertedChatRoom.toString())
                        // 데이터 삽입 성공
                        Toast.makeText(this@MakeRoomActivity, "모집방 개설 성공", Toast.LENGTH_SHORT).show()

                        val topic = insertedChatRoom?.roomId.toString()
                        //모집방id를 주제로 구독 -> 이후 서버에서 알림을 받을 수 있도록

                        Log.i("fcmmessage", topic)
                        if(topic.isNotEmpty()) {
                            FirebaseMessaging.getInstance().subscribeToTopic(topic)
                                .addOnSuccessListener {
                                    Toast.makeText(this@MakeRoomActivity, "구독성공", Toast.LENGTH_SHORT).show()
                                    Log.i("fcmmessage", "구독성공")

                                    //파이어베이스에도 자신을 참가자로 등록
                                    val job = CoroutineScope(Dispatchers.IO).launch{
                                        registerMyInfoIntoFirebase(insertedChatRoom?.roomId!!)
                                        val intent = Intent(this@MakeRoomActivity, MainActivity::class.java)
                                        startActivity(intent)
                                    }
                                }
                                .addOnFailureListener {
                                    Toast.makeText(this@MakeRoomActivity, "구독실패", Toast.LENGTH_SHORT).show()
                                    Log.i("fcmmessage", "구독실패")
                                }
                        }

//                        val intent = Intent(this@MakeRoomActivity, MainActivity::class.java)
//                        startActivity(intent)
                    } else if (response.code() == 400) { // 데이터 삽입 실패
                        // 400 error code (Bad Request)
                        Toast.makeText(this@MakeRoomActivity, "모집방 개설 실패. 에러 400", Toast.LENGTH_SHORT).show()
                    } else {
                        val errorCode = response.code()
                        // 400 error code 아닐 때
                        Toast.makeText(this@MakeRoomActivity, "모집방 개설 실패. 에러 $errorCode", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ChatRoom>, t: Throwable) {
                    // 네트워크 오류 처리
                    t.message?.let { it1 -> Log.i("onFailure", it1) }
                }
            })
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

        // 선택한 음식 종류 업데이트
        selectFoodType = when (selectedButton) {
            binding.KoreaF -> "한식"
            binding.JapanF -> "일식"
            binding.ForeignF -> "양식"
            binding.ChinaF -> "중식"
            binding.ectF -> "기타"
            else -> ""
        }
    }

    // 인원수 버튼 클릭 시 호출되는 함수
    private fun selectPersonType(selectedPersonType: Int) {
        selectPersonType = selectedPersonType

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
        when (selectedPersonType) {
            2 -> {
                binding.P2.backgroundTintList = getColorStateList(R.color.main)
                binding.P2.setTextColor(ContextCompat.getColor(this, R.color.white))
            }
            3 -> {
                binding.P3.backgroundTintList = getColorStateList(R.color.main)
                binding.P3.setTextColor(ContextCompat.getColor(this, R.color.white))
            }
            4 -> {
                binding.P4.backgroundTintList = getColorStateList(R.color.main)
                binding.P4.setTextColor(ContextCompat.getColor(this, R.color.white))
            }
            5 -> {
                binding.P5.backgroundTintList = getColorStateList(R.color.main)
                binding.P5.setTextColor(ContextCompat.getColor(this, R.color.white))
            }
            6 -> {
                binding.P6.backgroundTintList = getColorStateList(R.color.main)
                binding.P6.setTextColor(ContextCompat.getColor(this, R.color.white))
            }
        }
    }

    //내정보 가져오기
    private fun getMyInfoFromServer(): Member{
        val accessToken = SharedPreferences.getString("accessToken", "")
        //서버에서 내정보 가져오기
        val request = RetrofitClient.memberService.selectOne(
            "Bearer $accessToken")
        val response = request.execute()
        return response.body()!!
    }

    private val myInfo by lazy {
        getMyInfoFromServer()
    }

    //방에 입장할 떄 파이어베이스에도 자신의 정보 업데이트
    private fun registerMyInfoIntoFirebase(chatRoomId: Long) {
        val rf = Firebase.database.getReference("chatRoom/$chatRoomId/participants")

        //나를 참가자로 등록
        rf.child(myInfo.uid.toString()).setValue("")
    }
}