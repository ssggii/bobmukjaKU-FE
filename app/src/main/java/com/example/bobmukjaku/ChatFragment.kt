package com.example.bobmukjaku

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bobmukjaku.Model.*
import com.example.bobmukjaku.databinding.FragmentChatBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*

class ChatFragment : Fragment() {

    lateinit var mContext: Context
    lateinit var binding: FragmentChatBinding
    lateinit var adapter: ChatRoomAllListAdapter // 내 모집방 목록
    lateinit var adapter4: ChatRoomAllListAdapter // 전체 필터링
    var chatMyList = mutableListOf<ChatRoom>()
    var chatAllList = mutableListOf<ChatRoom>()
    var uid: Long = 0

    private val chatroomService = RetrofitClient.chatRoomService
    private val accessToken = SharedPreferences.getString("accessToken", "")
    private val authorizationHeader = "Bearer $accessToken"

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getUid()
        init()
        if (chatAllList != null) {
            setupSearchListener()
        } else {
            Toast.makeText(requireContext(), "검색할 모집방 목록이 존재하지 않습니다.", Toast.LENGTH_SHORT).show()
        }
//        getLatestSort()
        makeChatRoom()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun init() {
        getFilterFirstInfo() // 처음 정렬

        // 최신순, 오래된 순 정렬
        binding.sortBtn.setOnClickListener {
            if (binding.sortBtn.text == "최신순") {
                binding.sortBtn.text = "오래된순"
                val filter = FilterInfo("oldest", "")
                getFilterInfo(filter)
            }else {
                binding.sortBtn.text = "최신순"
                val filter = FilterInfo("latest", "")
                getFilterInfo(filter)
            }
        }

        // 시간표 필터링
        binding.ttBtn.setOnClickListener {
            if (binding.ttBtn.text == "시간표 ON") {
                binding.ttBtn.text = "시간표 OFF"

                val textColor = ContextCompat.getColor(requireContext(), R.color.black)
                val color = ContextCompat.getColor(requireContext(), R.color.gray)
                binding.ttBtn.setTextColor(textColor)
                binding.ttBtn.background.setColorFilter(color, PorterDuff.Mode.SRC_IN)
            }else {
                binding.ttBtn.text = "시간표 ON"
                val textColor = ContextCompat.getColor(requireContext(), R.color.white)
                val color = ContextCompat.getColor(requireContext(), R.color.main)
                binding.ttBtn.setTextColor(textColor)
                binding.ttBtn.background.setColorFilter(color, PorterDuff.Mode.SRC_IN)
            }
        }

        // 전체 필터링
        val whiteColor = ContextCompat.getColor(requireContext(), R.color.white)
        val blackColor = ContextCompat.getColor(requireContext(), R.color.black)
        val mainColor = ContextCompat.getColor(requireContext(), R.color.main)

        var selected = ""

        // 날짜 필터링
        binding.timeBtn.setOnClickListener {
            if (binding.dateFilter.visibility == View.GONE) {
                binding.dateFilter.visibility = View.VISIBLE
                binding.personFilter.visibility = View.GONE
                binding.foodFilter.visibility = View.GONE
            } else {
                binding.dateFilter.visibility = View.GONE
            }
        }

        binding.today.setOnClickListener {
            selected = LocalDate.now().toString()
            val filter = FilterInfo("meetingDate", selected)
            if (binding.today.currentTextColor == whiteColor) {
                removeFilterInfo(filter)
                unselectedDColor()
                binding.timeBtn.setTextColor(blackColor)
                binding.timeBtn.background.setColorFilter(whiteColor, PorterDuff.Mode.SRC_IN)
            }else {
                getFilterInfo(filter)
                unselectedDColor()
                selectedColor(binding.today)
                binding.timeBtn.setTextColor(whiteColor)
                binding.timeBtn.background.setColorFilter(mainColor, PorterDuff.Mode.SRC_IN)
            }
        }

        binding.tomorrow.setOnClickListener {
            selected = LocalDate.now().plusDays(1).toString()
            val filter = FilterInfo("meetingDate", selected)
            if (binding.tomorrow.currentTextColor == whiteColor) {
                removeFilterInfo(filter)
                unselectedDColor()
                binding.timeBtn.setTextColor(blackColor)
                binding.timeBtn.background.setColorFilter(whiteColor, PorterDuff.Mode.SRC_IN)
            }else {
                getFilterInfo(filter)
                unselectedDColor()
                selectedColor(binding.tomorrow)
                binding.timeBtn.setTextColor(whiteColor)
                binding.timeBtn.background.setColorFilter(mainColor, PorterDuff.Mode.SRC_IN)
            }
        }

        binding.otherDate.setOnClickListener {
            showDatePicker()
        }

        // 음식 필터링
        binding.foodBtn.setOnClickListener {
//            getFoodLists() // 테스트
            if (binding.foodFilter.visibility == View.GONE) {
                binding.foodFilter.visibility = View.VISIBLE
                binding.personFilter.visibility = View.GONE
                binding.dateFilter.visibility = View.GONE
            } else {
                binding.foodFilter.visibility = View.GONE
            }
        }

        binding.KoreaF.setOnClickListener {
            selected = "한식"
            val filter = FilterInfo("kindOfFood", selected)
            if (binding.KoreaF.currentTextColor == whiteColor) {
                removeFilterInfo(filter)
                unselectedColor()
                binding.foodBtn.setTextColor(blackColor)
                binding.foodBtn.background.setColorFilter(whiteColor, PorterDuff.Mode.SRC_IN)
            }else {
                getFilterInfo(filter)
                unselectedColor()
                selectedColor(binding.KoreaF)
                binding.foodBtn.setTextColor(whiteColor)
                binding.foodBtn.background.setColorFilter(mainColor, PorterDuff.Mode.SRC_IN)
            }
        }
        binding.JapanF.setOnClickListener {
            selected = "일식"
            val filter = FilterInfo("kindOfFood", selected)
            if (binding.JapanF.currentTextColor == whiteColor) {
                removeFilterInfo(filter)
                unselectedColor()
                binding.foodBtn.setTextColor(blackColor)
                binding.foodBtn.background.setColorFilter(whiteColor, PorterDuff.Mode.SRC_IN)
            }else {
                getFilterInfo(filter)
                unselectedColor()
                selectedColor(binding.JapanF)
                binding.foodBtn.setTextColor(whiteColor)
                binding.foodBtn.background.setColorFilter(mainColor, PorterDuff.Mode.SRC_IN)
            }
        }
        binding.ForeignF.setOnClickListener {
            selected = "양식"
            val filter = FilterInfo("kindOfFood", selected)
            if (binding.ForeignF.currentTextColor == whiteColor) {
                removeFilterInfo(filter)
                unselectedColor()
                binding.foodBtn.setTextColor(blackColor)
                binding.foodBtn.background.setColorFilter(whiteColor, PorterDuff.Mode.SRC_IN)
            }else {
                getFilterInfo(filter)
                unselectedColor()
                selectedColor(binding.ForeignF)
                binding.foodBtn.setTextColor(whiteColor)
                binding.foodBtn.background.setColorFilter(mainColor, PorterDuff.Mode.SRC_IN)
            }
        }
        binding.ChinaF.setOnClickListener {
            selected = "중식"
            val filter = FilterInfo("kindOfFood", selected)
            if (binding.ChinaF.currentTextColor == whiteColor) {
                removeFilterInfo(filter)
                unselectedColor()
                binding.foodBtn.setTextColor(blackColor)
                binding.foodBtn.background.setColorFilter(whiteColor, PorterDuff.Mode.SRC_IN)
            }else {
                getFilterInfo(filter)
                unselectedColor()
                selectedColor(binding.ChinaF)
                binding.foodBtn.setTextColor(whiteColor)
                binding.foodBtn.background.setColorFilter(mainColor, PorterDuff.Mode.SRC_IN)
            }
        }
        binding.ectF.setOnClickListener {
            selected = "기타"
            val filter = FilterInfo("kindOfFood", selected)
            if (binding.ectF.currentTextColor == whiteColor) {
                removeFilterInfo(filter)
                unselectedColor()
                binding.foodBtn.setTextColor(blackColor)
                binding.foodBtn.background.setColorFilter(whiteColor, PorterDuff.Mode.SRC_IN)
            }else {
                getFilterInfo(filter)
                unselectedColor()
                selectedColor(binding.ectF)
                binding.foodBtn.setTextColor(whiteColor)
                binding.foodBtn.background.setColorFilter(mainColor, PorterDuff.Mode.SRC_IN)
            }
        }

        // 인원 수 필터링
        binding.personBtn.setOnClickListener {
            if (binding.personFilter.visibility == View.GONE) {
                binding.personFilter.visibility = View.VISIBLE
                binding.dateFilter.visibility = View.GONE
                binding.foodFilter.visibility = View.GONE
            } else {
                binding.personFilter.visibility = View.GONE
            }
        }

        binding.P2.setOnClickListener {
            selected = "2"
            val filter = FilterInfo("total", selected)
            if (binding.P2.currentTextColor == whiteColor) {
                removeFilterInfo(filter)
                unselectedPColor()
                binding.personBtn.setTextColor(blackColor)
                binding.personBtn.background.setColorFilter(whiteColor, PorterDuff.Mode.SRC_IN)
            }else {
                getFilterInfo(filter)
                unselectedPColor()
                selectedColor(binding.P2)
                binding.personBtn.setTextColor(whiteColor)
                binding.personBtn.background.setColorFilter(mainColor, PorterDuff.Mode.SRC_IN)
            }
        }
        binding.P3.setOnClickListener {
            selected = "3"
            val filter = FilterInfo("total", selected)
            if (binding.P3.currentTextColor == whiteColor) {
                removeFilterInfo(filter)
                unselectedPColor()
                binding.personBtn.setTextColor(blackColor)
                binding.personBtn.background.setColorFilter(whiteColor, PorterDuff.Mode.SRC_IN)
            }else {
                getFilterInfo(filter)
                unselectedPColor()
                selectedColor(binding.P3)
                binding.personBtn.setTextColor(whiteColor)
                binding.personBtn.background.setColorFilter(mainColor, PorterDuff.Mode.SRC_IN)
            }
        }
        binding.P4.setOnClickListener {
            selected = "4"
            val filter = FilterInfo("total", selected)
            if (binding.P4.currentTextColor == whiteColor) {
                removeFilterInfo(filter)
                unselectedPColor()
                binding.personBtn.setTextColor(blackColor)
                binding.personBtn.background.setColorFilter(whiteColor, PorterDuff.Mode.SRC_IN)
            }else {
                getFilterInfo(filter)
                unselectedPColor()
                selectedColor(binding.P4)
                binding.personBtn.setTextColor(whiteColor)
                binding.personBtn.background.setColorFilter(mainColor, PorterDuff.Mode.SRC_IN)
            }
        }
        binding.P5.setOnClickListener {
            selected = "5"
            val filter = FilterInfo("total", selected)
            if (binding.P5.currentTextColor == whiteColor) {
                removeFilterInfo(filter)
                unselectedPColor()
                binding.personBtn.setTextColor(blackColor)
                binding.personBtn.background.setColorFilter(whiteColor, PorterDuff.Mode.SRC_IN)
            }else {
                getFilterInfo(filter)
                unselectedPColor()
                selectedColor(binding.P5)
                binding.personBtn.setTextColor(whiteColor)
                binding.personBtn.background.setColorFilter(mainColor, PorterDuff.Mode.SRC_IN)
            }
        }
        binding.P6.setOnClickListener {
            selected = "6"
            val filter = FilterInfo("total", selected)
            if (binding.P6.currentTextColor == whiteColor) {
                removeFilterInfo(filter)
                unselectedPColor()
                binding.personBtn.setTextColor(blackColor)
                binding.personBtn.background.setColorFilter(whiteColor, PorterDuff.Mode.SRC_IN)
            }else {
                getFilterInfo(filter)
                unselectedPColor()
                selectedColor(binding.P6)
                binding.personBtn.setTextColor(whiteColor)
                binding.personBtn.background.setColorFilter(mainColor, PorterDuff.Mode.SRC_IN)
            }
        }
    }

    // DatePicker 다이얼로그를 띄우는 함수
    @RequiresApi(Build.VERSION_CODES.O)
    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val whiteColor = ContextCompat.getColor(requireContext(), R.color.white)
        val blackColor = ContextCompat.getColor(requireContext(), R.color.black)
        val mainColor = ContextCompat.getColor(requireContext(), R.color.main)

        // 현재 날짜 기준 2주 뒤까지 날짜 선택 가능
        calendar.add(Calendar.WEEK_OF_YEAR, 2)
        val maxDate = calendar.timeInMillis

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
                val selected = formatDate(year, month, dayOfMonth)
                val filter = FilterInfo("meetingDate", selected)
                if (binding.otherDate.currentTextColor == whiteColor) {
                    removeFilterInfo(filter)
                    unselectedDColor()
                    binding.timeBtn.setTextColor(blackColor)
                    binding.timeBtn.background.setColorFilter(whiteColor, PorterDuff.Mode.SRC_IN)
                }else {
                    getFilterInfo(filter)
                    unselectedDColor()
                    selectedColor(binding.otherDate)
                    binding.timeBtn.setTextColor(whiteColor)
                    binding.timeBtn.background.setColorFilter(mainColor, PorterDuff.Mode.SRC_IN)
                }
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

    private fun selectedColor(selectedButton: AppCompatButton) {
        val textColor = ContextCompat.getColor(requireContext(), R.color.white)
        val color = ContextCompat.getColor(requireContext(), R.color.darkGray)
        selectedButton.setTextColor(textColor)
        selectedButton.background.setColorFilter(color, PorterDuff.Mode.SRC_IN)
    }

    private fun unselectedColor() {
        val foodList = listOf(
            binding.KoreaF,
            binding.JapanF,
            binding.ForeignF,
            binding.ChinaF,
            binding.ectF
        )
        val textColor = ContextCompat.getColor(requireContext(), R.color.white)
        val originalTextColor = ContextCompat.getColor(requireContext(), R.color.black)
        val color = ContextCompat.getColor(requireContext(), R.color.gray)
        for (food in foodList) {
            if (food.currentTextColor == textColor) {
                food.setTextColor(originalTextColor)
                food.background.setColorFilter(color, PorterDuff.Mode.SRC_IN)
            }
        }
    }

    private fun unselectedPColor() {
        val personList = listOf(
            binding.P2,
            binding.P3,
            binding.P4,
            binding.P5,
            binding.P6
        )
        val textColor = ContextCompat.getColor(requireContext(), R.color.white)
        val originalTextColor = ContextCompat.getColor(requireContext(), R.color.black)
        val color = ContextCompat.getColor(requireContext(), R.color.gray)
        for (person in personList) {
            if (person.currentTextColor == textColor) {
                person.setTextColor(originalTextColor)
                person.background.setColorFilter(color, PorterDuff.Mode.SRC_IN)
            }
        }
    }

    private fun unselectedDColor() {
        val dateList = listOf(
            binding.today,
            binding.tomorrow,
            binding.otherDate,
        )
        val textColor = ContextCompat.getColor(requireContext(), R.color.white)
        val originalTextColor = ContextCompat.getColor(requireContext(), R.color.black)
        val color = ContextCompat.getColor(requireContext(), R.color.gray)
        for (date in dateList) {
            if (date.currentTextColor == textColor) {
                date.setTextColor(originalTextColor)
                date.background.setColorFilter(color, PorterDuff.Mode.SRC_IN)
            }
        }
    }

    private fun makeChatRoom() {
        // 모집방 개설 버튼 클릭 이벤트 처리
        binding.openRoomBtn.setOnClickListener {
            val intent = Intent(requireContext(), MakeRoomActivity::class.java)
            startActivity(intent)
        }
    }

    private fun getFilteredLists(filters: List<FilterInfo>, excludedRooms: List<ChatRoom>) {
        Log.i("filterInfo:", filters.toString())
        val call = chatroomService.filteredLists(authorizationHeader, filters)
        call.enqueue(object : Callback<List<ChatRoom>> {
            override fun onResponse(call: Call<List<ChatRoom>>, response: Response<List<ChatRoom>>) {
                if (response.isSuccessful) {
                    val chatroomList = response.body()
                    if (chatroomList != null) {
                        chatAllList.clear()
                        chatAllList.addAll(chatroomList.filter { room -> room !in excludedRooms }) // 필터링된 목록에서 내 모집방 제거
                        adapter4.updateItems(chatAllList)

                        val successCode = response.code()
                        Toast.makeText(requireContext(), "전체 필터링 성공 $successCode $chatroomList", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    val errorCode = response.code()
                    if (errorCode == 404) {
                        chatAllList.clear()
                        adapter4.updateItems(chatAllList)
                    }else {
                        Toast.makeText(requireContext(), "전체 필터링 실패. 에러 $errorCode", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call<List<ChatRoom>>, t: Throwable) {
                // 네트워크 오류 또는 기타 에러가 발생했을 때의 처리
                t.message?.let { it1 -> Log.i("[전체 필터링 에러: ]", it1) }
            }
        })

        binding.allRecyclerView.layoutManager = LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false)
        adapter4 = ChatRoomAllListAdapter(chatAllList)
        adapter4.onItemClickListener = object : ChatRoomAllListAdapter.OnItemClickListener {
            override fun onItemClick(pos: Int, roomInfo: ChatRoom) {
                joinChatRoomDialog(roomInfo)
            }
        }
        binding.allRecyclerView.adapter = adapter4
    }

    private fun getFilterInfo(newFilter: FilterInfo) {
        val call = chatroomService.getFilter(authorizationHeader)
        call.enqueue(object : Callback<List<FilterInfo>> {
            override fun onResponse(
                call: Call<List<FilterInfo>>,
                response: Response<List<FilterInfo>>
            ) {
                if (response.isSuccessful) {
                    val filterInfo = response.body()
                    val finalFilters = mutableListOf<FilterInfo>()
                    if (filterInfo != null) {
                        if (newFilter.filterType == "oldest") {
                            for (filter in filterInfo) {
                                if (filter.filterType != "latest") {
                                    finalFilters.add(filter)
                                }
                            }
                        } else if (newFilter.filterType == "latest") {
                            for (filter in filterInfo) {
                                if (filter.filterType != "oldest") {
                                    finalFilters.add(filter)
                                }
                            }
                        } else {
                            // newFilter의 타입과 다른 필터만 finalFilters에 추가
                            for (filter in filterInfo) {
                                if (filter.filterType != newFilter.filterType) {
                                    finalFilters.add(filter)
                                }
                            }
                        }
                    }
                    finalFilters.add(newFilter)
                    getFilteredLists(finalFilters, chatMyList)
                } else {
                    val errorCode = response.code()
                    Toast.makeText(
                        requireContext(),
                        "필터 정보 로드 실패. 에러 코드: $errorCode",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<List<FilterInfo>>, t: Throwable) {
                // 네트워크 오류 또는 기타 에러가 발생했을 때의 처리
                t.message?.let { it1 -> Log.i("[필터 정보 로드 기타 에러: ]", it1) }
            }
        })
    }

    private fun removeFilterInfo(removeFilter: FilterInfo) {
        val call = chatroomService.getFilter(authorizationHeader)
        call.enqueue(object : Callback<List<FilterInfo>> {
            override fun onResponse(
                call: Call<List<FilterInfo>>,
                response: Response<List<FilterInfo>>
            ) {
                if (response.isSuccessful) {
                    val filterInfo = response.body()
                    val finalFilters = mutableListOf<FilterInfo>()
                    if (filterInfo != null) {
                        // newFilter의 타입과 다른 필터만 finalFilters에 추가
                        for (filter in filterInfo) {
                            if (filter.filterType != removeFilter.filterType) {
                                finalFilters.add(filter)
                            }
                        }
                    }
                    getFilteredLists(finalFilters, chatMyList)
                } else {
                    val errorCode = response.code()
                    Toast.makeText(
                        requireContext(),
                        "필터 제거 실패. 에러 코드: $errorCode",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<List<FilterInfo>>, t: Throwable) {
                // 네트워크 오류 또는 기타 에러가 발생했을 때의 처리
                t.message?.let { it1 -> Log.i("[필터 제거 기타 에러: ]", it1) }
            }
        })
    }

    private fun setupSearchListener() {
        val searchEditText = binding.contentSearch

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // 사용하지 않음
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (::adapter4.isInitialized) { // adapter4가 초기화되었을 때만 업데이트
                    // 입력된 텍스트를 기반으로 필터링된 목록을 가져와서 업데이트
                    val filteredList = chatAllList.filter { room -> room.roomName?.contains(s.toString()) ?: false }
                    adapter4.updateItems(filteredList)
                }
            }

            override fun afterTextChanged(s: Editable?) {
                // 사용하지 않음
            }
        })
    }

    private fun getFilterFirstInfo() {
        val call = chatroomService.getFilter(authorizationHeader)
        call.enqueue(object : Callback<List<FilterInfo>> {
            override fun onResponse(
                call: Call<List<FilterInfo>>,
                response: Response<List<FilterInfo>>
            ) {
                if (response.isSuccessful) {
                    // FilterInfo 정보 있으면 반환, 없으면 최신순 필터 적용
                    val filterInfo = response.body()
                    Log.i("FirstFilterInfo", filterInfo.toString())
                    if (filterInfo != null) {
                        if (filterInfo.toString() == "[]") {
                            val filters = listOf(
                                FilterInfo("latest", "")
                            )
                            Log.i("[]", "빈 리스트")
                            getFilteredLists(filters, chatMyList)
                        } else {
                            Log.i("[FilterFirstInfo]", "반환 성공")
                            for (filters in filterInfo) {
                                val whiteColor = ContextCompat.getColor(requireContext(), R.color.white)
                                val mainColor = ContextCompat.getColor(requireContext(), R.color.main)

                                if (filters.filterType == "oldest") {
                                    binding.sortBtn.text = "오래된순"
                                } else if (filters.filterType == "kindOfFood") {
                                    binding.foodBtn.setTextColor(whiteColor)
                                    binding.foodBtn.background.setColorFilter(mainColor, PorterDuff.Mode.SRC_IN)
                                    when (filters.filterValue) {
                                        "한식" -> selectedColor(binding.KoreaF)
                                        "일식" -> selectedColor(binding.JapanF)
                                        "양식" -> selectedColor(binding.ForeignF)
                                        "중식" -> selectedColor(binding.ChinaF)
                                        "기타" -> selectedColor(binding.ectF)
                                    }
                                } else if (filters.filterType == "total") {
                                    binding.personBtn.setTextColor(whiteColor)
                                    binding.personBtn.background.setColorFilter(mainColor, PorterDuff.Mode.SRC_IN)
                                    when (filters.filterValue) {
                                        "2" -> selectedColor(binding.P2)
                                        "3" -> selectedColor(binding.P3)
                                        "4" -> selectedColor(binding.P4)
                                        "5" -> selectedColor(binding.P5)
                                        "6" -> selectedColor(binding.P6)
                                    }
                                } else if (filters.filterType == "meetingDate") {
                                    binding.timeBtn.setTextColor(whiteColor)
                                    binding.timeBtn.background.setColorFilter(mainColor, PorterDuff.Mode.SRC_IN)
                                    when (filters.filterValue) {
                                        "오늘" -> selectedColor(binding.today)
                                        "내일" -> selectedColor(binding.tomorrow)
                                        else -> selectedColor(binding.otherDate)
                                    }
                                }
                            }
                            getFilteredLists(filterInfo, chatMyList)
                        }
                    } else {
                        val filters = listOf(
                            FilterInfo("latest", "")
                        )
                        Log.i("[FilterFirstInfo]", "null 반환")
                        getFilteredLists(filters, chatMyList)
                    }
                } else {
                    val errorCode = response.code()
                    Toast.makeText(
                        requireContext(),
                        "필터 정보 첫 로드 실패. 에러 코드: $errorCode",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<List<FilterInfo>>, t: Throwable) {
                // 네트워크 오류 또는 기타 에러가 발생했을 때의 처리
                t.message?.let { it1 -> Log.i("[필터 정보 첫 로드 기타 에러: ]", it1) }
            }
        })
    }

    private fun getChatRoomMyList() {
        val call = chatroomService.getMyLists(authorizationHeader, uid)
        call.enqueue(object : Callback<List<ChatRoom>> {
            override fun onResponse(call: Call<List<ChatRoom>>, response: Response<List<ChatRoom>>) {
                if (response.isSuccessful) {
                    val chatroomList = response.body()
                    if (chatroomList != null) {
                        chatMyList.addAll(chatroomList)
                        adapter.updateItems(chatroomList)

                        val successCode = response.code()
                        Toast.makeText(requireContext(), "내 모집방 목록 로드. 성공 $successCode $uid", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    val errorCode = response.code()
                    Toast.makeText(requireContext(), "내 모집방 목록 로드 실패. 에러 $errorCode", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<ChatRoom>>, t: Throwable) {
                // 네트워크 오류 또는 기타 에러가 발생했을 때의 처리
                t.message?.let { it1 -> Log.i("[내 모집방 목록 로드 에러: ]", it1) }
            }
        })

        binding.joinRecyclerView.layoutManager = LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false)
        adapter = ChatRoomAllListAdapter(chatMyList)
        adapter.onItemClickListener = object : ChatRoomAllListAdapter.OnItemClickListener {
            override fun onItemClick(pos: Int, roomInfo: ChatRoom) {
                val intent = Intent(requireContext(), ChatActivity::class.java)
                intent.putExtra("uid", uid)
                intent.putExtra("roomId", roomInfo.roomId)
                intent.putExtra("roomName", roomInfo.roomName)
                intent.putExtra("meetingDate", roomInfo.meetingDate)
                intent.putExtra("startTime", roomInfo.startTime)
                intent.putExtra("endTime", roomInfo.endTime)
                intent.putExtra("kindOfFood", roomInfo.kindOfFood)
                intent.putExtra("total", roomInfo.total)
                intent.putExtra("currentNum", roomInfo.currentNum)
                startActivity(intent)
            }
        }
        binding.joinRecyclerView.adapter = adapter
    }

    private fun joinChatRoomDialog(roomInfo: ChatRoom) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.join_chatroom_dialog, null)
        val yesButton = dialogView.findViewById<TextView>(R.id.time_btn_yes)
        val noButton = dialogView.findViewById<TextView>(R.id.time_btn_no)

        val builder = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(false)

        val alertDialog = builder.create()
        alertDialog.show()

        // 확인 버튼 클릭 이벤트 처리
        yesButton.setOnClickListener {
            val addMember = AddChatRoomMember(roomId = roomInfo.roomId, uid = uid)
            val call = chatroomService.addMember(authorizationHeader, addMember)
            call.enqueue(object : Callback<ServerBooleanResponse> {
                override fun onResponse(call: Call<ServerBooleanResponse>, response: Response<ServerBooleanResponse>) {
                    if (response.isSuccessful) {
                        val serverResponse = response.body()
                        if (serverResponse != null && serverResponse.success) {
                            // 서버 응답이 true일 경우 처리
                            val intent = Intent(requireContext(), ChatActivity::class.java)
                            intent.putExtra("uid", uid)
                            intent.putExtra("roomId", roomInfo.roomId)
                            intent.putExtra("roomName", roomInfo.roomName)
                            intent.putExtra("meetingDate", roomInfo.meetingDate)
                            intent.putExtra("startTime", roomInfo.startTime)
                            intent.putExtra("endTime", roomInfo.endTime)
                            intent.putExtra("kindOfFood", roomInfo.kindOfFood)
                            intent.putExtra("total", roomInfo.total)
                            intent.putExtra("currentNum", roomInfo.currentNum?.plus(1))
                            startActivity(intent)
                        } else {
                            // 서버 응답이 false일 경우 처리
                            val errorCode = response.code()
                            Toast.makeText(
                                requireContext(),
                                "모집방 입장 실패 (정원 초과)",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        // 서버 응답이 실패일 경우 처리
                        val errorCode = response.code()
                        Toast.makeText(
                            requireContext(),
                            "모집방 입장 실패. 에러 코드: $errorCode",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<ServerBooleanResponse>, t: Throwable) {
                    // 네트워크 오류 또는 기타 에러가 발생했을 때의 처리
                    t.message?.let { it1 -> Log.i("[모집방 입장 실패: ]", it1) }
                }
            })
            alertDialog.dismiss()
        }

        // 취소 버튼 클릭 이벤트 처리
        noButton.setOnClickListener {
            alertDialog.dismiss()
        }
    }

    private fun getUid() {
        val memberService = RetrofitClient.memberService

        val call = accessToken?.let { memberService.selectOne(authorizationHeader) }
        call?.enqueue(object : Callback<Member> {
            override fun onResponse(call: Call<Member>, response: Response<Member>) {
                if (response.isSuccessful) {
                    val member = response.body()
                    val uidInfo = member?.uid
                    if (uidInfo != null) {
                        uid = uidInfo
                        getChatRoomMyList()
                    }
                } else {
                    val errorCode = response.code()
                    Toast.makeText(
                        requireContext(),
                        "uid를 가져오는데 실패했습니다. 에러 코드: $errorCode",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<Member>, t: Throwable) {
                // 네트워크 오류 또는 기타 에러가 발생했을 때의 처리
                t.message?.let { it1 -> Log.i("[uid 로드 실패: ]", it1) }
            }
        })
    }
}