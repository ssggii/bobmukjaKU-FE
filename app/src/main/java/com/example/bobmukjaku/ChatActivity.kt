package com.example.bobmukjaku

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bobmukjaku.Model.ChatModel
import com.example.bobmukjaku.Model.RetrofitClient
import com.example.bobmukjaku.databinding.ActivityChatBinding
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChatActivity : AppCompatActivity() {
    lateinit var binding: ActivityChatBinding
    lateinit var adapter:ChatAdapter

    lateinit var myName:String
    lateinit var yourName:String


    var chatItem:ArrayList<ChatModel> = arrayListOf<ChatModel>()


    val chatRoomId = "testChatRoomId"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initData()
        initLayout()
        initRecyclerView()
    }

    private fun initData() {
        myName = "kim"//
        //yourName = intent.getStringExtra("name")!!



//        Firebase.database.getReference("users/$myUid/username")
//            .get()
//            .addOnSuccessListener {
//                myName = it.value.toString()
//            }


        //채팅방id를 토대로 이전까지 주고받았던 메시지를 파이어베이스로부터 가져와서 recyclerView에 반영
        Firebase.database.getReference("message/$chatRoomId")
            .get()
            .addOnSuccessListener{
                chatItem.clear()
                for (chat in it.children) {
                    val message = chat.child("message").value.toString()
                    val senderName = chat.child("senderName").value.toString()
                    val time = chat.child("time").value.toString().toLong()
                    val isShareMessage = chat.child("isShareMessage").value.toString().toBoolean()
                    val chatRoomIdFromMessage = chat.child("chatRoomId").value.toString()
                    chatItem.add(ChatModel(message, senderName, time, isShareMessage, chatRoomIdFromMessage))
                }
                adapter.notifyDataSetChanged()

            }

        //파이어베이스의 채팅방에 메시지가 업데이트되면 이를 반영
        val rf = Firebase.database.getReference("message/$chatRoomId")
        val childEventListener = object: ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                //Log.i("chat", snapshot.toString())
                val message = snapshot.child("message").value.toString()
                val senderUid = snapshot.child("senderUid").value.toString()
                val senderName = snapshot.child("senderName").value.toString()
                val time = snapshot.child("time").value.toString().toLong()
                val isShareMessage = snapshot.child("isShareMessage").value.toString()
                val chatRoomIdFromMessage = snapshot.child("chatRoomId").value.toString()
                chatItem.add(ChatModel(message, senderName, time, isShareMessage = false, chatRoomIdFromMessage))
                adapter.notifyDataSetChanged()
                binding.chatRecyclerView.scrollToPosition(chatItem.size - 1)

            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                TODO("Not yet implemented")
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                TODO("Not yet implemented")
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                TODO("Not yet implemented")
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }


        }
        rf.addChildEventListener(childEventListener)

    }

    private fun initLayout() {

        binding.apply {
            sendMsg.setOnClickListener {

//                //스와이프할때 메뉴탭이 열리거나 닫히지 않도록 lock으로 초기화
//                menuDrawer.setDrawerLockMode(LOCK_MODE_LOCKED_CLOSED)
//
//                //오른쪽 상단 메뉴버튼 눌렀을 때
//                chatroomMenu.setOnClickListener {
//                    menuDrawer.openDrawer(Gravity.RIGHT)
//                }

                //message전송
                sendMsg.setOnClickListener {
                    //메시지 전송 버튼을 누르면 firebase의 현재 채팅방경로에 메시지 내용을 추가

                    val message = this.message.text.toString()
                    this.message.setText("")

                    //val service = retrofit.create(UserApi::class.java)
                    val request = RetrofitClient.memberService.sendMessage(
                        ChatModel(message,
                            myName,
                            System.currentTimeMillis(),
                            false,
                            chatRoomId)
                    )

                    request.enqueue(object: Callback<Unit>{
                        override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                            Toast.makeText(this@ChatActivity, "메시지 전송 성공", Toast.LENGTH_SHORT).show()
                        }

                        override fun onFailure(call: Call<Unit>, t: Throwable) {
                            Toast.makeText(this@ChatActivity, "메시지 전송 실패", Toast.LENGTH_SHORT).show()
                        }

                    })


//                    FirebaseMessaging.getInstance().subscribeToTopic("111111")
//                        .addOnCompleteListener {task->
//                            var msg = "Subscribed"
//                            if(!task.isSuccessful){
//                                msg = "Subscribed failed"
//                            }
//                            Log.i("kim", msg)
//                            Toast.makeText(this@ChatActivity, msg, Toast.LENGTH_SHORT).show()
//                        }
//                        .addOnCanceledListener {
//                            Log.i("kim", "canceled")
//                        }
                }

            }
        }
    }


    private fun initRecyclerView() {

        val layoutManager = LinearLayoutManager(this@ChatActivity, LinearLayoutManager.VERTICAL, false)
        layoutManager.stackFromEnd = true
        binding.chatRecyclerView.layoutManager = layoutManager
        adapter = ChatAdapter(chatItem)
        binding.chatRecyclerView.adapter = adapter
        binding.chatRecyclerView.scrollToPosition(chatItem.size - 1)
    }
}