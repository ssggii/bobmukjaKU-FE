package com.example.bobmukjaku

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

class ChatFragment : Fragment() {

    lateinit var mContext: Context
    lateinit var binding: FragmentChatBinding
    lateinit var adapter: ChatRoomListAdapter
    lateinit var adapter2: ChatRoomAllListAdapter
    var chatlist = arrayListOf<UserItem>()
    var chatAllList = mutableListOf<ChatRoom>()

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

        getChatRoomList()
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

    private fun getChatRoomList() {
        val db = Firebase.database.getReference("users")
        db.get().addOnSuccessListener { dataSnapshot: DataSnapshot ->
            for (user in dataSnapshot.children) {
                val name = user.child("username").value.toString()
                val uid = user.child("uid").value.toString()
                Log.i("user", name.plus(uid))
                chatlist.add(UserItem(name, "message", uid))
            }

                binding.joinRecyclerView.layoutManager = LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false)
                adapter = ChatRoomListAdapter(chatlist)
                adapter.onItemClickListener = object:ChatRoomListAdapter.OnItemClickListener{
                    override fun onItemClick(pos: Int) {
                        val intent = Intent(requireActivity(), ChatActivity::class.java)
                        intent.putExtra("name", chatlist[pos].name)
                        intent.putExtra("uid", chatlist[pos].uid)
                        startActivity(intent)
                    }
                }
                binding.joinRecyclerView.adapter = adapter
            }
    }

    private fun getChatRoomAllList() {
        val accessToken = SharedPreferences.getString("accessToken", "")
        val authorizationHeader = "Bearer $accessToken"

        val chatroomService = RetrofitClient.chatRoomService

        val call = chatroomService.setLists(authorizationHeader)
        call.enqueue(object : Callback<List<ChatRoom>> {
            override fun onResponse(call: Call<List<ChatRoom>>, response: Response<List<ChatRoom>>) {
                if (response.isSuccessful) {
                    val chatroomList = response.body()
                    if (chatroomList != null) {
                        val roomId = chatroomList.map { it.roomId }
                        val roomName = chatroomList.map { it.roomName }
                        val meetingDate = chatroomList.map { it.meetingDate }
                        val startTime = chatroomList.map { it.startTime }
                        val endTime = chatroomList.map { it.endTime }
                        val kindOfFood = chatroomList.map { it.kindOfFood }
                        val total = chatroomList.map { it.total }
                        val currentNum = chatroomList.map {it.currentNum}
                        chatAllList.addAll(chatroomList)
                        adapter2.updateItems(chatroomList)
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
            adapter2 = ChatRoomAllListAdapter(chatAllList)
            adapter2.onItemClickListener = object:ChatRoomAllListAdapter.OnItemClickListener{
                override fun onItemClick(pos: Int) {
                }
            }
            binding.allRecyclerView.adapter = adapter2
    }
}