package com.example.bobmukjaku

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bobmukjaku.Model.*
import com.example.bobmukjaku.databinding.FragmentChatBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.properties.Delegates

class ChatFragment : Fragment() {

    lateinit var mContext: Context
    lateinit var binding: FragmentChatBinding
    lateinit var adapter: ChatRoomAllListAdapter
    lateinit var adapter2: ChatRoomAllListAdapter
    lateinit var adapter3: ChatRoomAllListAdapter
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

        binding.sortBtn.setOnClickListener {
            getLatestSort() // 테스트용 위치
        }
        binding.foodBtn.setOnClickListener {
            getFoodLists() // 테스트
        }

        getChatRoomAllList()
        makeChatRoom()
    }

    private fun makeChatRoom() {
        // 모집방 개설 버튼 클릭 이벤트 처리
        binding.openRoomBtn.setOnClickListener {
            val intent = Intent(requireContext(), MakeRoomActivity::class.java)
            startActivity(intent)
        }
    }

    private fun getFoodLists() {
        val call = chatroomService.getFoodLists(authorizationHeader, "한식")
        call.enqueue(object : Callback<List<ChatRoom>> {
            override fun onResponse(call: Call<List<ChatRoom>>, response: Response<List<ChatRoom>>) {
                if (response.isSuccessful) {
                    val chatroomList = response.body()
                    if (chatroomList != null) {
                        chatMyList.addAll(chatroomList)
                        adapter.updateItems(chatroomList)

                        val successCode = response.code()
                        Toast.makeText(requireContext(), "내 모집방 목록 로드. 성공 $successCode $chatroomList", Toast.LENGTH_SHORT).show()
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

        binding.allRecyclerView.layoutManager = LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false)
        adapter = ChatRoomAllListAdapter(chatMyList)
        adapter.onItemClickListener = object : ChatRoomAllListAdapter.OnItemClickListener {
            override fun onItemClick(pos: Int, roomInfo: ChatRoom) {
                joinChatRoomDialog(roomInfo)
            }
        }
        binding.joinRecyclerView.adapter = adapter
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
                intent.putExtra("currentNum", roomInfo.currentNum?.plus(1))
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

    private fun getChatRoomAllList() {
        val call = chatroomService.setLists(authorizationHeader)
        call.enqueue(object : Callback<List<ChatRoom>> {
            override fun onResponse(call: Call<List<ChatRoom>>, response: Response<List<ChatRoom>>) {
                if (response.isSuccessful) {
                    val chatroomList = response.body()
                    if (chatroomList != null) {
                        chatAllList.addAll(chatroomList)
                        adapter3.updateItems(chatroomList)
                    }
                } else {
                    val errorCode = response.code()
                    Toast.makeText(requireContext(), "모집방 목록 로드 실패. 에러 $errorCode", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<ChatRoom>>, t: Throwable) {
                // 네트워크 오류 또는 기타 에러가 발생했을 때의 처리
                t.message?.let { it1 -> Log.i("[모집방 목록 로드 에러: ]", it1) }
            }
        })

            binding.allRecyclerView.layoutManager = LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false)
            adapter3 = ChatRoomAllListAdapter(chatAllList)
            adapter3.onItemClickListener = object : ChatRoomAllListAdapter.OnItemClickListener {
                override fun onItemClick(pos: Int, roomInfo: ChatRoom) {
                    joinChatRoomDialog(roomInfo)
                }
            }
            binding.allRecyclerView.adapter = adapter3
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