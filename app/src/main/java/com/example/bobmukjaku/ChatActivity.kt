package com.example.bobmukjaku

import android.os.Build
import android.os.Bundle
import android.util.TypedValue
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
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class ChatActivity : AppCompatActivity() {
    lateinit var binding: ActivityChatBinding
    lateinit var adapter:ChatAdapter
    lateinit var adapter2: ChatMenuParticipantsAdapter

    lateinit var myName:String
    private val chatRoomInfo by lazy{
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra("data",ChatRoom::class.java)
        } else {
            intent.getSerializableExtra("data") as ChatRoom?
        }
    }


    var chatItem:ArrayList<ChatModel> = arrayListOf<ChatModel>()


    //val chatRoomId = "testChatRoomId"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initData()
        initLayout()
        initRecyclerView()
        //initParticipantInMenu()
    }

    private fun initData() {
        myName = "kim"//



        //채팅방id를 토대로 이전까지 주고받았던 메시지를 파이어베이스로부터 가져와서 recyclerView에 반영
        Firebase.database.getReference("chatRoom/${chatRoomInfo?.roomId}/message")
            .get()
            .addOnSuccessListener{
                chatItem.clear()
                for (chat in it.children) {
                    val message = chat.child("message").value.toString()
                    val senderName = chat.child("senderName").value.toString()
                    val time = chat.child("time").value.toString().toLong()
                    val isShareMessage = chat.child("isShareMessage").value.toString().toBoolean()
                    val chatRoomIdFromMessage = chat.child("chatRoomId").value.toString().toLong()
                    chatItem.add(ChatModel(message, senderName, time, isShareMessage, chatRoomIdFromMessage))
                }
                adapter.notifyDataSetChanged()

            }

        //파이어베이스의 채팅방에 메시지가 업데이트되면 이를 반영
        val rf = Firebase.database.getReference("chatRoom/${chatRoomInfo?.roomId}/message")
        val childEventListener = object: ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                //Log.i("chat", snapshot.toString())
                val message = snapshot.child("message").value.toString()
                val senderUid = snapshot.child("senderUid").value.toString()
                val senderName = snapshot.child("senderName").value.toString()
                val time = snapshot.child("time").value.toString().toLong()
                val isShareMessage = snapshot.child("isShareMessage").value.toString()
                val chatRoomIdFromMessage = snapshot.child("chatRoomId").value.toString().toLong()
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

                    //val service = retrofit.create(UserApi::class.java)
                    val request = RetrofitClient.memberService.sendMessage(
                        ChatModel(message,
                            myName,
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

        //채팅 RecyclerView초기화
        val layoutManager = LinearLayoutManager(this@ChatActivity, LinearLayoutManager.VERTICAL, false)
        layoutManager.stackFromEnd = true
        binding.chatRecyclerView.layoutManager = layoutManager
        adapter = ChatAdapter(chatItem)
        binding.chatRecyclerView.adapter = adapter
        binding.chatRecyclerView.scrollToPosition(chatItem.size - 1)

        //메뉴recyclerView 초기화
        val layoutManager2 = LinearLayoutManager(this@ChatActivity, LinearLayoutManager.VERTICAL, false)
        binding.menuRecyclerview.layoutManager = layoutManager2
        val list = arrayListOf<WrapperInChatRoomMenu>()
        list.add(WrapperInChatRoomMenu(1,Member(null,null, null, null, null, null, null)))
        list.add(WrapperInChatRoomMenu(2,Member(1,"aaaa", "aaaa", "밥묵자쿠1", "2020-02-02", 3, "#141")))
        list.add(WrapperInChatRoomMenu(2,Member(1,"aaaa", "aaaa", "밥묵자쿠1", "2020-02-02", 3, "#141")))
        list.add(WrapperInChatRoomMenu(2,Member(1,"aaaa", "aaaa", "밥묵자쿠1", "2020-02-02", 3, "#141")))
        list.add(WrapperInChatRoomMenu(2,Member(1,"aaaa", "aaaa", "밥묵자쿠1", "2020-02-02", 3, "#141")))
        list.add(WrapperInChatRoomMenu(2,Member(1,"aaaa", "aaaa", "밥묵자쿠1", "2020-02-02", 3, "#141")))
        list.add(WrapperInChatRoomMenu(2,Member(1,"aaaa", "aaaa", "밥묵자쿠1", "2020-02-02", 3, "#141")))
        list.add(WrapperInChatRoomMenu(2,Member(1,"aaaa", "aaaa", "밥묵자쿠1", "2020-02-02", 3, "#141")))
        list.add(WrapperInChatRoomMenu(2,Member(1,"aaaa", "aaaa", "밥묵자쿠1", "2020-02-02", 3, "#141")))
        list.add(WrapperInChatRoomMenu(2,Member(1,"aaaa", "aaaa", "밥묵자쿠1", "2020-02-02", 3, "#141")))
        list.add(WrapperInChatRoomMenu(2,Member(1,"aaaa", "aaaa", "밥묵자쿠1", "2020-02-02", 3, "#141")))
        list.add(WrapperInChatRoomMenu(2,Member(1,"aaaa", "aaaa", "밥묵자쿠1", "2020-02-02", 3, "#141")))
        list.add(WrapperInChatRoomMenu(2,Member(1,"aaaa", "aaaa", "밥묵자쿠1", "2020-02-02", 3, "#141")))
        list.add(WrapperInChatRoomMenu(2,Member(1,"aaaa", "aaaa", "밥묵자쿠1", "2020-02-02", 3, "#141")))
        list.add(WrapperInChatRoomMenu(2,Member(1,"aaaa", "aaaa", "밥묵자쿠1", "2020-02-02", 3, "#141")))
        list.add(WrapperInChatRoomMenu(2,Member(1,"aaaa", "aaaa", "밥묵자쿠1", "2020-02-02", 3, "#141")))
        list.add(WrapperInChatRoomMenu(2,Member(1,"aaaa", "aaaa", "밥묵자쿠1", "2020-02-02", 3, "#141")))
        list.add(WrapperInChatRoomMenu(2,Member(1,"aaaa", "aaaa", "밥묵자쿠1", "2020-02-02", 3, "#141")))
        list.add(WrapperInChatRoomMenu(2,Member(1,"aaaa", "aaaa", "밥묵자쿠1", "2020-02-02", 3, "#141")))
        list.add(WrapperInChatRoomMenu(2,Member(1,"aaaa", "aaaa", "밥묵자쿠1", "2020-02-02", 3, "#141")))
        list.add(WrapperInChatRoomMenu(2,Member(1,"aaaa", "aaaa", "밥묵자쿠1", "2020-02-02", 3, "#141")))
        list.add(WrapperInChatRoomMenu(2,Member(1,"aaaa", "aaaa", "밥묵자쿠1", "2020-02-02", 3, "#141")))
        list.add(WrapperInChatRoomMenu(2,Member(1,"aaaa", "aaaa", "밥묵자쿠1", "2020-02-02", 3, "#141")))
        list.add(WrapperInChatRoomMenu(2,Member(1,"aaaa", "aaaa", "밥묵자쿠1", "2020-02-02", 3, "#141")))
        list.add(WrapperInChatRoomMenu(2,Member(1,"aaaa", "aaaa", "밥묵자쿠1", "2020-02-02", 3, "#141")))
        list.add(WrapperInChatRoomMenu(2,Member(1,"aaaa", "aaaa", "밥묵자쿠1", "2020-02-02", 3, "#141")))
        list.add(WrapperInChatRoomMenu(2,Member(1,"aaaa", "aaaa", "밥묵자쿠1", "2020-02-02", 3, "#141")))
        list.add(WrapperInChatRoomMenu(2,Member(1,"aaaa", "aaaa", "밥묵자쿠1", "2020-02-02", 3, "#141")))
        list.add(WrapperInChatRoomMenu(2,Member(1,"aaaa", "aaaa", "밥묵자쿠1", "2020-02-02", 3, "#141")))
        list.add(WrapperInChatRoomMenu(2,Member(1,"aaaa", "aaaa", "밥묵자쿠1", "2020-02-02", 3, "#141")))
        list.add(WrapperInChatRoomMenu(2,Member(1,"aaaa", "aaaa", "밥묵자쿠1", "2020-02-02", 3, "#141")))


        adapter2 = ChatMenuParticipantsAdapter(list)
        binding.menuRecyclerview.adapter = adapter2

    }




    //메뉴에서 참여자 관련 초기화 코드
/*    private fun initParticipantInMenu(){
        //방목록화면에서 참여자 수를 넘겨받았다고 가정
        val participantsNum = 5
        val uidList = arrayListOf<Int>()
        uidList.add(1)
        uidList.add(2)
        uidList.add(3)
        uidList.add(4)
        uidList.add(5)

        //참가자 수 만큼 뷰생성
        for(i in 0 until participantsNum) {
            //LinearLayout 생성
            var linearLayoutParticipant = LinearLayout(this)
            var layoutParamsForLinearLayout = LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            linearLayoutParticipant.id = uidList[i]
            layoutParamsForLinearLayout.topMargin = getFloatToDp(10f)
            linearLayoutParticipant.layoutParams = layoutParamsForLinearLayout
            linearLayoutParticipant.orientation = LinearLayout.HORIZONTAL


            //참가자 프로필 이미지 뷰 생성하고 LinearLayout에 add
            var imageParticipant = ImageButton(this)
            var layoutParams = LayoutParams(getFloatToDp(40f), getFloatToDp(40f))
            layoutParams.leftMargin = getFloatToDp(30f)
            imageParticipant.layoutParams = layoutParams
            imageParticipant.background = ContextCompat.getDrawable(this, R.drawable.ku_3)
            linearLayoutParticipant.addView(imageParticipant)

            //LinearLayout도 add
            binding.menuParticipants.addView(linearLayoutParticipant)
        }

        binding.menuParticipants.removeView(findViewById(uidList[1]))


    }*/

    private fun getFloatToDp(float: Float): Int {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, float, resources.displayMetrics)
            .toInt()
    }
}