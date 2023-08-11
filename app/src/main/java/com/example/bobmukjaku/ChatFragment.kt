package com.example.bobmukjaku

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bobmukjaku.Model.*
import com.example.bobmukjaku.databinding.FragmentChatBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChatFragment : Fragment() {

    lateinit var mContext: Context
    lateinit var binding: FragmentChatBinding
    lateinit var adapter: ChatRoomAllListAdapter // 내 모집방 목록
    lateinit var adapter2: ChatRoomAllListAdapter // 최신순
    lateinit var adapter3: ChatRoomAllListAdapter // 오래된 순
    lateinit var adapter4: ChatRoomAllListAdapter // 전체 필터링
    var chatMyList = mutableListOf<ChatRoom>()
    var chatLatestList = mutableListOf<ChatRoom>()
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getUid()
        init()
        getLatestSort()
        makeChatRoom()
    }

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
//                val filters = listOf(
//                    FilterInfo("latest", "")
//                )
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

        // 날짜 필터링
        binding.timeBtn.setOnClickListener {
            val filters = listOf(
                FilterInfo("meetingDate", "2023-08-12")
            )
            getFilteredLists(filters)
        }

        // 전체 필터링
        val whiteColor = ContextCompat.getColor(requireContext(), R.color.white)
        val blackColor = ContextCompat.getColor(requireContext(), R.color.black)
        val mainColor = ContextCompat.getColor(requireContext(), R.color.main)

        var selected = ""

        // 음식 필터링
        binding.foodBtn.setOnClickListener {
//            getFoodLists() // 테스트
            if (binding.foodFilter.visibility == View.GONE) {
                binding.foodFilter.visibility = View.VISIBLE
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

    private fun makeChatRoom() {
        // 모집방 개설 버튼 클릭 이벤트 처리
        binding.openRoomBtn.setOnClickListener {
            val intent = Intent(requireContext(), MakeRoomActivity::class.java)
            startActivity(intent)
        }
    }

    private fun getFilteredLists(filters: List<FilterInfo>) {
        val call = chatroomService.filteredLists(authorizationHeader, filters)
        call.enqueue(object : Callback<List<ChatRoom>> {
            override fun onResponse(call: Call<List<ChatRoom>>, response: Response<List<ChatRoom>>) {
                if (response.isSuccessful) {
                    val chatroomList = response.body()
                    if (chatroomList != null) {
                        chatAllList.clear()
                        chatAllList.addAll(chatroomList)
                        adapter4.updateItems(chatroomList)

                        val successCode = response.code()
                        Toast.makeText(requireContext(), "전체 필터링(음식) 성공 $successCode $chatroomList", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    val errorCode = response.code()
                    Toast.makeText(requireContext(), "전체 필터링(음식) 실패. 에러 $errorCode", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<ChatRoom>>, t: Throwable) {
                // 네트워크 오류 또는 기타 에러가 발생했을 때의 처리
                t.message?.let { it1 -> Log.i("[전체 필터링(음식) 에러: ]", it1) }
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
                    getFilteredLists(finalFilters)
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
                    getFilteredLists(finalFilters)
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
                    if (filterInfo != null) {
                        getFilteredLists(filterInfo)
                    } else {
                        val filters = listOf(
                            FilterInfo("latest", "")
                        )
                        getFilteredLists(filters)
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

//        val db = Firebase.database.getReference("users")
//        db.get().addOnSuccessListener { dataSnapshot: DataSnapshot ->
//            for (user in dataSnapshot.children) {
//                val name = user.child("username").value.toString()
//                val uid = user.child("uid").value.toString()
//                Log.i("user", name.plus(uid))
//                chatlist.add(UserItem(name, "message", uid))
//            }
//
//                binding.joinRecyclerView.layoutManager = LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false)
//                adapter = ChatRoomListAdapter(chatlist)
//                adapter.onItemClickListener = object:ChatRoomListAdapter.OnItemClickListener{
//                    override fun onItemClick(pos: Int) {
//                        val intent = Intent(requireActivity(), ChatActivity::class.java)
//                        intent.putExtra("name", chatlist[pos].name)
//                        intent.putExtra("uid", chatlist[pos].uid)
//                        startActivity(intent)
//                    }
//                }
//                binding.joinRecyclerView.adapter = adapter
//            }
    }

    private fun getLatestSort() {
        val call = chatroomService.getLatestLists(authorizationHeader)
        call.enqueue(object : Callback<List<ChatRoom>> {
            override fun onResponse(call: Call<List<ChatRoom>>, response: Response<List<ChatRoom>>) {
                if (response.isSuccessful) {
                    val chatroomList = response.body()
                    if (chatroomList != null) {
                        chatLatestList.addAll(chatroomList)
                        adapter2.updateItems(chatroomList)
                    }
                } else {
                    val errorCode = response.code()
                    Toast.makeText(requireContext(), "모집방 최신 목록 로드 실패. 에러 $errorCode", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<ChatRoom>>, t: Throwable) {
                // 네트워크 오류 또는 기타 에러가 발생했을 때의 처리
                t.message?.let { it1 -> Log.i("[모집방 최신 목록 로드 에러: ]", it1) }
            }
        })

        binding.allRecyclerView.layoutManager = LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false)
        adapter2 = ChatRoomAllListAdapter(chatLatestList)
        adapter2.onItemClickListener = object : ChatRoomAllListAdapter.OnItemClickListener {
            override fun onItemClick(pos: Int, roomInfo: ChatRoom) {
                joinChatRoomDialog(roomInfo)
            }
        }
        binding.allRecyclerView.adapter = adapter2
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

            val intent = Intent(requireContext(), ChatActivity::class.java)
            startActivity(intent)
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