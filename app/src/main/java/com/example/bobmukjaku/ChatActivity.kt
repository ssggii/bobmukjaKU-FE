package com.example.bobmukjaku

import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout.LOCK_MODE_LOCKED_CLOSED
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bobmukjaku.Model.*
import com.example.bobmukjaku.databinding.ActivityChatBinding
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class ChatActivity : AppCompatActivity() {
    lateinit var binding: ActivityChatBinding
    lateinit var adapter:ChatAdapter
    lateinit var adapter2: ChatMenuParticipantsAdapter

    lateinit var myInfo: Member//내정보
    private val chatRoomInfo by lazy{//현재 방 정보
        ChatRoom(intent.getLongExtra("roomId", -1),
            intent.getStringExtra("roomName"),
            intent.getStringExtra("meetingDate"),
            intent.getStringExtra("startTime"),
            intent.getStringExtra("endTime"),
            intent.getStringExtra("kindOfFood"),
            intent.getIntExtra("total", -1),
            intent.getIntExtra("currentNum", -1)
        )
    }
    var chatItem:ArrayList<ChatModel> = arrayListOf<ChatModel>()//채팅 저장 배열
    var participantsMenuList = arrayListOf<WrapperInChatRoomMenu>()//참가자 목록 저장 배열
    private val rf by lazy {Firebase.database.getReference("chatRoom/${chatRoomInfo?.roomId}")}



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)


        CoroutineScope(Dispatchers.Main).launch {
            val job = CoroutineScope(Dispatchers.IO).async {
                getMyInfo()
            }.await()
            initFirebase()
            registerMyInfoIntoFirebase()
            initRecyclerView()
            initLayout()
        }
    }

    private fun registerMyInfoIntoFirebase() {
        val mrf = rf.child("participants")

        //나를 참가자로 등록
        mrf.child(myInfo.uid.toString()).setValue(myInfo)

        val childEventListener = object: ChildEventListener{
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                //다른 누군가가 채팅방에 입장
                val participantUid = snapshot.child("uid").value.toString().toLong()
                val participantName = snapshot.child("memberNickName").value.toString()
                val participantRate = snapshot.child("rate").value.toString().toInt()
                val paricipantProfileColor = snapshot.child("profileColor").value.toString()
                //나머지 정보는 필요없을 듯

                val participantInfo = Member(
                    participantUid,
                    null,
                    null,
                    participantName,
                    null,
                    participantRate,
                    paricipantProfileColor)

                participantsMenuList.add(WrapperInChatRoomMenu(2, participantInfo))
                adapter2.notifyDataSetChanged()
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {
                participantsMenuList.removeIf {
                    Log.i("kim", snapshot.key?:"null")
                    it.member.uid.toString() == snapshot.key
                }
                adapter2.notifyDataSetChanged()
            }
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {}
        }
        mrf.addChildEventListener(childEventListener)
    }

    private fun initFirebase() {
        //채팅방id를 토대로 이전까지 주고받았던 메시지를 파이어베이스로부터 가져와서 recyclerView에 반영
        //val rf = Firebase.database.getReference("chatRoom/${chatRoomInfo?.roomId}/message")
        val mrf = rf.child("message")

        chatItem.clear()
        //파이어베이스의 채팅방에 메시지가 업데이트되면 이를 반영
        val childEventListener = object: ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                //Log.i("chat", snapshot.toString())
                val message = snapshot.child("message").value.toString()
                val senderUid = snapshot.child("senderUid").value.toString().toLong()
                val senderName = snapshot.child("senderName").value.toString()
                val time = snapshot.child("time").value.toString().toLong()
                val isShareMessage = snapshot.child("isShareMessage").value.toString().toBoolean()
                val chatRoomIdFromMessage = snapshot.child("chatRoomId").value.toString().toLong()
                chatItem.add(ChatModel(message, senderUid, senderName, time, isShareMessage, chatRoomIdFromMessage))
                adapter.notifyDataSetChanged()
                binding.chatRecyclerView.scrollToPosition(chatItem.size - 1)
            }
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {}
        }

        mrf.addChildEventListener(childEventListener)
    }

    private fun getMyInfo(){
        val accessToken = SharedPreferences.getString("accessToken", "")
        //서버에서 내정보 가져오기
        val request = RetrofitClient.memberService.selectOne(
            "Bearer $accessToken")
        val response = request.execute()
        if(response.isSuccessful) {
            myInfo = response.body()!!
        }
    }

    private fun initLayout() {

        binding.apply {

            //채팅방 퇴장 버튼
            exitBtn.setOnClickListener {
                //파이어베이스에서 자신의 정보 제거
                rf.child("participants/${myInfo.uid}").removeValue().addOnSuccessListener {
                    Toast.makeText(this@ChatActivity, "퇴장완료", Toast.LENGTH_SHORT).show()
                }

                //서버에도 퇴장 요청
            }

            //공지화면으로
            setBobAppointment.setOnClickListener {

            }

            //스와이프할때 메뉴탭이 열리거나 닫히지 않도록 lock으로 초기화
            menuDrawer.setDrawerLockMode(LOCK_MODE_LOCKED_CLOSED)

            //오른쪽 상단 메뉴버튼 눌렀을 때
            chatroomMenu.setOnClickListener {
                menuDrawer.openDrawer(Gravity.RIGHT)
            }

            sendMsg.setOnClickListener {



                //message전송
                sendMsg.setOnClickListener {
                    //메시지 전송 버튼을 누르면 firebase의 현재 채팅방경로에 메시지 내용을 추가

                    val message = this.message.text.toString()
                    this.message.setText("")
                    val accessToken = SharedPreferences.getString("accessToken", "")!!

                    //val service = retrofit.create(UserApi::class.java)
                    val request = RetrofitClient.memberService.sendMessage(
                        "Bearer ${accessToken}",
                        ChatModel(message,
                            myInfo.uid,
                            myInfo.memberNickName,
                            System.currentTimeMillis(),
                            false,
                            chatRoomInfo?.roomId)
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
        Log.i("kim", "BBB")
        //채팅 RecyclerView초기화
        val layoutManager = LinearLayoutManager(this@ChatActivity, LinearLayoutManager.VERTICAL, false)
        layoutManager.stackFromEnd = true
        binding.chatRecyclerView.layoutManager = layoutManager
        adapter = ChatAdapter(chatItem, myInfo)
        binding.chatRecyclerView.adapter = adapter
        binding.chatRecyclerView.scrollToPosition(chatItem.size - 1)

        //메뉴recyclerView 초기화
        val layoutManager2 = LinearLayoutManager(this@ChatActivity, LinearLayoutManager.VERTICAL, false)
        binding.menuRecyclerview.layoutManager = layoutManager2
        participantsMenuList.add(WrapperInChatRoomMenu(1,Member(null,null, null, null, null, null, null)))
//        participants.add(WrapperInChatRoomMenu(2,Member(1,"aaaa", "aaaa", "밥묵자쿠1", "2020-02-02", 3, "#141")))
//        participants.add(WrapperInChatRoomMenu(2,Member(1,"aaaa", "aaaa", "밥묵자쿠1", "2020-02-02", 3, "#141")))
//        participants.add(WrapperInChatRoomMenu(2,Member(1,"aaaa", "aaaa", "밥묵자쿠1", "2020-02-02", 3, "#141")))
//        participants.add(WrapperInChatRoomMenu(2,Member(1,"aaaa", "aaaa", "밥묵자쿠1", "2020-02-02", 3, "#141")))
        adapter2 = ChatMenuParticipantsAdapter(participantsMenuList, this@ChatActivity)
        binding.menuRecyclerview.adapter = adapter2

    }
}





