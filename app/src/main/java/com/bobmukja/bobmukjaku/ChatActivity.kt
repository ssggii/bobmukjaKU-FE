package com.bobmukja.bobmukjaku

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout.LOCK_MODE_LOCKED_CLOSED
import androidx.recyclerview.widget.LinearLayoutManager
import com.bobmukja.bobmukjaku.Dto.BlockInfoDto
import com.bobmukja.bobmukjaku.Dto.FriendInfoDto
import com.bobmukja.bobmukjaku.Dto.FriendUpdateDto
import com.bobmukja.bobmukjaku.Dto.NameRateBgDto
import com.bobmukja.bobmukjaku.Model.*
import com.bobmukja.bobmukjaku.databinding.ActivityChatBinding
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*


class ChatActivity : AppCompatActivity() {
    lateinit var binding: ActivityChatBinding
    lateinit var adapter:ChatAdapter
    lateinit var adapter2: ChatMenuParticipantsAdapter
    val accessToken by lazy{ SharedPreferences.getString("accessToken", "") }
    val participantListForChatAdapter = arrayListOf<Member>()

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

    override fun onDestroy() {
        super.onDestroy()
        Log.i("aaa", "destroy")
    }

    var chatItem:ArrayList<ChatModel> = arrayListOf<ChatModel>()//채팅 저장 배열
    var participantsMenuList = arrayListOf<WrapperInChatRoomMenu>()//참가자 목록 저장 배열
    private val rf by lazy {Firebase.database.getReference("chatRoom/${chatRoomInfo?.roomId}")}

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        Log.i("iii", intent?.getStringExtra("place")?:"null")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)


        Log.i("roomId", chatRoomInfo.roomId.toString())


        CoroutineScope(Dispatchers.Main).launch {
            val job =
                CoroutineScope(Dispatchers.IO).async {
                    myInfo = getMyInfoFromServer()
                }.await()

            val job2 =
                CoroutineScope(Dispatchers.IO).async {
                    getParticipantsListFromServer()
                    initFirebaseMenuParticipants()
                }.await()
            initFirebase()
            initNotice()
            initLayout()
            initRecyclerView()
        }
    }

    private fun getParticipantsListFromServer(){
        val request = RetrofitClient.memberService.getParticipantsInRoom("Bearer $accessToken", chatRoomInfo.roomId!! )
        val response = request.execute()
        if(response.isSuccessful){
            val participants = response.body()!!

            //참여자 정보 가져온 후 recyclerView초기화
            participantListForChatAdapter.clear()
            for( member in participants){
                participantListForChatAdapter.add(member)
                val uid = member.uid
                rf.child("participants/$uid").setValue("")
            }
        }
            /*.enqueue(object: Callback<List<Member>>{
                override fun onResponse(
                    call: Call<List<Member>>,
                    response: Response<List<Member>>
                ) {
                    //서버에서 모든 참여자 정보를 가져와서 파이어베이스에 등록
                    if(response.isSuccessful)
                    {
                        val participants = response.body()!!

                        //참여자 정보 가져온 후 recyclerView초기화
                        participantListForChatAdapter.clear()
                        for( member in participants){
                            //participantListForChatAdapter.add(member)
                            val uid = member.uid
                            rf.child("participants/$uid").setValue("")
                        }


                    }
                }

                override fun onFailure(call: Call<List<Member>>, t: Throwable) {
                    TODO("Not yet implemented")
                }

            })*/
    }

    fun initNotice(){
        val mrf = rf.child("notice")

        val childEventListener = object:ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.i("noticefb", snapshot.value.toString())

                // 음식점 공유 메시지 성공적으로 들어오는 거 테스트
                val placeName = intent.getStringExtra("placeName")
                val placeAddress = intent.getStringExtra("placeAddress")
                val imageUrl = intent.getStringExtra("imageUrl")

                Log.i("sharedRestaurantTest", "$placeName | $placeAddress | $imageUrl")
                // 음식점 공유 메시지 성공적으로 들어오는 거 테스트

                val noticeValue = snapshot.value
                if(noticeValue != null){
                    val restaurantName = snapshot.child("restaurantName").value.toString()
                    val startTime = snapshot.child("starttime").value.toString()
                    binding.noticeContent.text = "${chatRoomInfo.meetingDate} $startTime\n$restaurantName"

                    //채팅방 서랍의 약속정보도 동기화
                    val restaurantTextView = findViewById<TextView>(R.id.real_place)
                    val startTimeTextView = findViewById<TextView>(R.id.real_starttime)
                    if(restaurantTextView != null && startTimeTextView != null){
                        restaurantTextView.text = restaurantName
                        startTimeTextView.text = startTime
                    }
                }else{
                    binding.noticeContent.text = "밥약속 설정"

                    val restaurantTextView = findViewById<TextView>(R.id.real_place)
                    val startTimeTextView = findViewById<TextView>(R.id.real_starttime)
                    if(restaurantTextView != null && startTimeTextView != null){
                        restaurantTextView.text = "-"
                        startTimeTextView.text = "-"
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        }
        mrf.addValueEventListener(childEventListener)
    }

    private fun initFirebaseMenuParticipants() {
        val mrf = rf.child("participants")

        //나를 참가자로 등록
        //mrf.child(myInfo.uid.toString()).setValue(myInfo)

        val childEventListener = object: ChildEventListener{
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {

                //다른 누군가가 채팅방에 입장
                val participantUid = snapshot.key.toString().toLong()
                //val participantName = snapshot.child("memberNickName").value.toString()
                //val participantRate = snapshot.child("rate").value.toString().toInt()
                //val paricipantProfileColor = snapshot.child("profileColor").value.toString()
                //Log.i("kim", "childAdded $participantName")
                //var friendOrBlock = snapshot.child("friendOrBlock").value.toString()
                //나머지 정보는 필요없을 듯

                RetrofitClient.memberService.getNameRateBg(participantUid).enqueue(object:Callback<NameRateBgDto>{
                    override fun onResponse(
                        call: Call<NameRateBgDto>,
                        response: Response<NameRateBgDto>
                    ) {
                        Log.i("ttt", "$participantUid/${response.code()}")
                        val result = response.body()!!
                        if(participantListForChatAdapter.find { it.uid == participantUid } == null)
                            participantListForChatAdapter.add(Member(participantUid,null, null, result.name,null, result.rate, result.bg))
                        Log.i("hhh", participantListForChatAdapter.toString())



                        var blockList : List<BlockInfoDto>
                        var friendList : List<FriendInfoDto>
                        var friendOrBlock = "na"
                        //if(friendOrBlock.isNullOrBlank()) friendOrBlock="na"//na -> 친구도 차단하지도 않음(해당사항 없음)
                        RetrofitClient.friendService.getFriendList("Bearer $accessToken")
                            .enqueue(object: Callback<List<FriendInfoDto>>{
                                override fun onResponse(
                                    call: Call<List<FriendInfoDto>>,
                                    response: Response<List<FriendInfoDto>>
                                ) {
                                    //Log.i("kkkkkk", "여기까지")
                                    if(response.code() == 200 || response.code() == 204){
                                        when(response.code()){
                                            200->{
                                                friendList = response.body()!!
                                                for (item in friendList) {
                                                    if (item.friendUid == participantUid){
                                                        friendOrBlock = "friend"
                                                        break
                                                    }
                                                }
                                            }
                                            204->{
                                                Log.i("aaaaaa", "친구0명")
                                            }
                                        }
                                        //Log.i("kkkkkk", "여기까지")
                                        //if (friendOrBlock != "friend"){
                                        RetrofitClient.friendService.getBlockList("Bearer $accessToken")
                                            .enqueue(object : Callback<List<BlockInfoDto>> {
                                                override fun onResponse(
                                                    call: Call<List<BlockInfoDto>>,
                                                    response: Response<List<BlockInfoDto>>
                                                ) {
                                                    if(response.code() == 200 || response.code() == 204){
                                                        when(response.code()){
                                                            200->{
                                                                blockList = response.body()!!
                                                                //Log.i("kkkkkk", "여기까지")
                                                                for(item in blockList){
                                                                    if(item.blockUid == participantUid) {
                                                                        friendOrBlock = "block"
                                                                        break
                                                                    }
                                                                }
                                                            }
                                                            204->{
                                                                Log.i("aaaaaa", "차단0명")
                                                            }
                                                        }
                                                        Log.i("kkkkkk", "여기까지")
                                                        if(participantUid != myInfo.uid) {
                                                            val participantInfo = Member(
                                                                participantUid,
                                                                null,
                                                                null,
                                                                result.name,
                                                                null,
                                                                result.rate,
                                                                result.bg
                                                            )

                                                            participantsMenuList.add(
                                                                WrapperInChatRoomMenu(2,participantInfo,friendOrBlock)
                                                            )
                                                            Log.i("participant", participantsMenuList.toString())
                                                            adapter2.notifyDataSetChanged()
                                                        }
                                                    }
                                                }

                                                override fun onFailure(
                                                    call: Call<List<BlockInfoDto>>,
                                                    t: Throwable
                                                ) {
                                                    Log.i("aaaaaa", t.message.toString())
                                                }
                                            })
                                        //}
                                    }
                                }

                                override fun onFailure(call: Call<List<FriendInfoDto>>, t: Throwable) {
                                    Log.i("aaaaaa", t.message.toString())
                                }
                            })
                    }

                    override fun onFailure(call: Call<NameRateBgDto>, t: Throwable) {
                        TODO("Not yet implemented")
                    }

                })



            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {
                participantsMenuList.removeIf {
                    Log.i("kim", snapshot.key?:"null")
                    it.member.uid.toString() == snapshot.key

                }
                adapter2.notifyDataSetChanged()

                participantListForChatAdapter.removeIf { it.uid == snapshot.key?:-1 }

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
                val isShareMessage = snapshot.child("shareMessage").value.toString().toBoolean()
                val chatRoomIdFromMessage = snapshot.child("chatRoomId").value.toString().toLong()
                val isProfanity = snapshot.child("profanity").value.toString().toBoolean()
                //Log.i("kim", "$message&&$isShareMessage")


                chatItem.add(ChatModel(message, senderUid, senderName, time, isShareMessage, chatRoomIdFromMessage, isProfanity))
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


    private fun getMyInfoFromServer(): Member{
        //val accessToken = SharedPreferences.getString("accessToken", "")
        //서버에서 내정보 가져오기
        val request = RetrofitClient.memberService.selectOne(
            "Bearer $accessToken")
        val response = request.execute()
        return response.body()!!
    }

    val appointLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()){
        if(it.resultCode == Activity.RESULT_OK){
            //공지설정화면에서 공지를 설정하면 일어나는 일
        }
    }

    private fun exitChatRoomDialog(){
        val dialogView = LayoutInflater.from(this).inflate(R.layout.join_chatroom_dialog, null)
        val yesButton = dialogView.findViewById<TextView>(R.id.time_btn_yes)
        val noButton = dialogView.findViewById<TextView>(R.id.time_btn_no)
        val text = dialogView.findViewById<TextView>(R.id.guide_text)
        text.text = "퇴장하시겠습니까?"

        val builder = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)

        val alertDialog = builder.create()
        alertDialog.show()

        yesButton.setOnClickListener{

            exitChatRoom()
            alertDialog.dismiss()
        }

        noButton.setOnClickListener{
            alertDialog.dismiss()
        }

    }

    private fun exitChatRoom(){
        rf.child("participants/${myInfo.uid}").removeValue().addOnSuccessListener{
            val exitBody = AddChatRoomMember(chatRoomInfo.roomId, myInfo.uid)
            val request = RetrofitClient.chatRoomService.exitChatRoom("Bearer $accessToken", exitBody)
            request.enqueue(object: Callback<ServerBooleanResponse>{
                override fun onResponse(
                    call: Call<ServerBooleanResponse>,
                    response: Response<ServerBooleanResponse>
                ) {
                    if(response.isSuccessful){
                        when(response.code()){
                            200->{
                                FirebaseMessaging.getInstance()
                                    .unsubscribeFromTopic(chatRoomInfo.roomId.toString())
                                    .addOnSuccessListener {
                                        rf.child("participants").get().addOnSuccessListener {
                                            //Log.i("participantsNum", it.childrenCount.toString())
                                            if(it.childrenCount.toInt() == 0){
                                                //Log.i("participants", it.childrenCount.toString())
                                                rf.removeValue().addOnSuccessListener {
                                                    Log.i("exittt", "퇴장완료")
                                                    Toast.makeText(this@ChatActivity, "${chatRoomInfo.roomId}퇴장완료", Toast.LENGTH_SHORT).show()
                                                    val intent = Intent(this@ChatActivity, MainActivity::class.java)
                                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                                    startActivity(intent)
                                                }
                                            }else{
                                                Log.i("exittt", "퇴장완료")
                                                Toast.makeText(this@ChatActivity, "${chatRoomInfo.roomId}퇴장완료", Toast.LENGTH_SHORT).show()
                                                val intent = Intent(this@ChatActivity, MainActivity::class.java)
                                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                                startActivity(intent)
                                            }
                                        }
                                    }
                            }
                            else -> {
                                Log.i("exittt", "퇴장에러")
                                Toast.makeText(this@ChatActivity, "퇴장에러", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }else {
                        Log.i("exittt", response.code().toString())
                    }
                }

                override fun onFailure(call: Call<ServerBooleanResponse>, t: Throwable) {
                    Toast.makeText(this@ChatActivity, "퇴장실패", Toast.LENGTH_SHORT).show()
                }

            })
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun initLayout() {

        binding.apply {

            //모집방정보를 토대로 TextView를 초기화
            chatRoomName.text = chatRoomInfo.roomName

            //채팅방 퇴장 버튼
            exitBtn.setOnClickListener {
                exitChatRoomDialog()
            }

            //메인화면으로 나가기 버튼
            backBtn.setOnClickListener{
                val intent = Intent(this@ChatActivity, MainActivity::class.java)
                //val intent = Intent(this@ChatActivity, GiveScoreActivity::class.java)
                //intent.putExtra("roomId", 1L)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
            }

            //공지화면으로
            setBobAppointment.setOnClickListener {
                val intent = Intent(this@ChatActivity, BobAppointmentActivity::class.java)
                intent.putExtra("roomId", chatRoomInfo.roomId)
                intent.putExtra("meetingDate", chatRoomInfo.meetingDate)
                intent.putExtra("starttime", chatRoomInfo.startTime)
                startActivity(intent)
            }

            //스와이프할때 메뉴탭이 열리거나 닫히지 않도록 lock으로 초기화
            menuDrawer.setDrawerLockMode(LOCK_MODE_LOCKED_CLOSED)

            //오른쪽 상단 메뉴버튼 눌렀을 때
            chatroomMenu.setOnClickListener {
                menuDrawer.openDrawer(Gravity.RIGHT)
            }

            message.addTextChangedListener(object:TextWatcher{
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {

                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                }

                override fun afterTextChanged(s: Editable?) {
                    if(s?.isEmpty() == true){
                        binding.sendMsg.setImageDrawable(getDrawable(R.drawable.map_restaurnt_marker))
                    }else{
                        binding.sendMsg.setImageDrawable(getDrawable(R.drawable.msg_send))
                    }
                }
            })

            val calendar = Calendar.getInstance()
            //message전송
            sendMsg.setOnClickListener {
                //입력폼에 텍스트를 하나라도 입력하면 전송 비튼 역할, 아니면 맛지도 버튼 역할
                //메시지 전송 버튼을 누르면 firebase의 현재 채팅방경로에 메시지 내용을 추가

                calendar.timeInMillis = System.currentTimeMillis()
                val year = calendar[Calendar.YEAR]
                val month = calendar[Calendar.MONTH]
                val day = calendar[Calendar.DAY_OF_MONTH]

                val result = chatItem.find{
                    val calendar = Calendar.getInstance()
                    calendar.timeInMillis = it.time?:0
                    val yearInArray = calendar[Calendar.YEAR]
                    val monthInArray = calendar[Calendar.MONTH]
                    val dayInArray = calendar[Calendar.DAY_OF_MONTH]
                    (year==yearInArray)&&(month==monthInArray)&&(day==dayInArray)
                }

                if(result == null){//현재날짜에 보내는 최초의 메시지이므로 날짜를 표시하는 메시지를 먼저 전송
                    // 여기서 MapFragment를 시작
                    val mapIntent = Intent(this@ChatActivity, MainActivity::class.java)
                    mapIntent.putExtra("selectedItemId", R.id.second) // R.id.second는 MapFragment를 나타내는 탭 아이템입니다.
                    mapIntent.putExtra("roomId", chatRoomInfo.roomId)
                    mapIntent.putExtra("roomName", chatRoomInfo.roomName)
                    mapIntent.putExtra("meetingDate", chatRoomInfo.meetingDate)
                    mapIntent.putExtra("startTime", chatRoomInfo.startTime)
                    mapIntent.putExtra("endTime", chatRoomInfo.endTime)
                    mapIntent.putExtra("kindOfFood", chatRoomInfo.kindOfFood)
                    mapIntent.putExtra("total", chatRoomInfo.total)
                    mapIntent.putExtra("currentNum", chatRoomInfo.currentNum)

                    // shareMessageLauncher를 사용하여 MapFragment로 전환
                    //shareMessageLauncher.launch(mapIntent)

                    val accessToken = SharedPreferences.getString("accessToken", "")!!
                    RetrofitClient.memberService.sendMessage("Bearer $accessToken",
                        ChatModel("", -100, myInfo.memberNickName,
                            System.currentTimeMillis(), false,chatRoomInfo.roomId,false)
                    ).enqueue(object:Callback<Unit>{
                        override fun onResponse(call: Call<Unit>, response: Response<Unit>) {

                            val message = binding.message.text.toString()

                            if (message.isNotEmpty()){
                                sendMessage(message, false)//일반 메시지 전송
                            }else{
                                //입력폼에 텍스트를 입력하지 않았으므로 현재는 맛지도 버튼 역할
//                                val mapListFragment = MapListFragment()
//                                supportFragmentManager.beginTransaction()
//                                    .replace(R.id.map_container, mapListFragment)
//                                    .addToBackStack(null) // 필요에 따라 back stack에 추가
//                                    .commit()

                                shareMessageLauncher.launch(mapIntent)//맛지도화면으로 전환 -> 콜백함수에서 음식점 공유메시지 전송 수행
                            }
                        }

                        override fun onFailure(call: Call<Unit>, t: Throwable) {
                            TODO("Not yet implemented")
                        }

                    })
                }else{
                    val message = binding.message.text.toString()

                    if (message.isNotEmpty()){
                        sendMessage(message, false)//일반 메시지 전송
                    }else{
                        //입력폼에 텍스트를 입력하지 않았으므로 현재는 맛지도 버튼 역할
                        val mapIntent = Intent(this@ChatActivity, MainActivity::class.java)
                        mapIntent.putExtra("selectedItemId", R.id.second) // R.id.second는 MapFragment를 나타내는 탭 아이템입니다.
                        mapIntent.putExtra("roomId", chatRoomInfo.roomId)
                        mapIntent.putExtra("roomName", chatRoomInfo.roomName)
                        mapIntent.putExtra("meetingDate", chatRoomInfo.meetingDate)
                        mapIntent.putExtra("startTime", chatRoomInfo.startTime)
                        mapIntent.putExtra("endTime", chatRoomInfo.endTime)
                        mapIntent.putExtra("kindOfFood", chatRoomInfo.kindOfFood)
                        mapIntent.putExtra("total", chatRoomInfo.total)
                        mapIntent.putExtra("currentNum", chatRoomInfo.currentNum)

                        // shareMessageLauncher를 사용하여 MapFragment로 전환
                        shareMessageLauncher.launch(mapIntent)
                    }
                }
            }
        }
    }


    //일반 메시지 전송 메서드
    private fun sendMessage(message: String, isSharingMessage: Boolean){

        binding.message.setText("")
        val accessToken = SharedPreferences.getString("accessToken", "")!!

        Log.i("kim", isSharingMessage.toString())
        val request = RetrofitClient.memberService.sendMessage(
            "Bearer $accessToken",
            ChatModel(
                message,
                myInfo.uid,
                myInfo.memberNickName,
                System.currentTimeMillis(),
                isSharingMessage,
                chatRoomInfo?.roomId,
                profanity = false
            )
        )
        request.enqueue(object : Callback<Unit> {
            override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                Toast.makeText(this@ChatActivity, "메시지 전송 성공", Toast.LENGTH_SHORT).show()
            }
            override fun onFailure(call: Call<Unit>, t: Throwable) {
                Toast.makeText(this@ChatActivity, "메시지 전송 실패", Toast.LENGTH_SHORT).show()
            }
        })
    }

    //맛지도에서 음식점 공유메시지를 누르고 ChatActivity로 돌아왔을 때 수행할 콜백 등록
    private val shareMessageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        if(it.resultCode == Activity.RESULT_OK){
            val placeName = it.data?.getStringExtra("placeName")
            val placeAddress = it.data?.getStringExtra("placeAddress")
            val imageUrl = it.data?.getStringExtra("imageUrl")
            val countScrap = it.data?.getStringExtra("countScrap")?:"0"
            val reviewList = it.data?.getStringExtra("reviewList")?:""
            Log.i("kimsend", "$placeName| $placeAddress | $imageUrl | $countScrap | $reviewList")

            //음식점 공유메시지 전송
            //sendMessage("", true, RestaurantInfoForShareMessage(placeName, placeAddress, imageUrl))
            val message = "$placeName|$placeAddress|$imageUrl|$countScrap|$reviewList"
            sendMessage(message, true)
        }
    }




    private fun initRecyclerView() {

        //채팅 RecyclerView초기화
        val layoutManager = LinearLayoutManager(this@ChatActivity, LinearLayoutManager.VERTICAL, false)
        //layoutManager.stackFromEnd = true
        binding.chatRecyclerView.layoutManager = layoutManager


        adapter = ChatAdapter(chatItem, myInfo, participantListForChatAdapter)
        binding.chatRecyclerView.adapter = adapter
        binding.chatRecyclerView.scrollToPosition(chatItem.size - 1)
        Log.i("aaaa", "chatActivity : $participantsMenuList")

        //메뉴recyclerView 초기화
        val layoutManager2 = LinearLayoutManager(this@ChatActivity, LinearLayoutManager.VERTICAL, false)
        binding.menuRecyclerview.layoutManager = layoutManager2

        //메뉴창상단 표시용 add
        participantsMenuList.add(WrapperInChatRoomMenu(1,Member(null,null, null, null, null, null, null), ""))
        adapter2 = ChatMenuParticipantsAdapter(participantsMenuList, this@ChatActivity, chatRoomInfo)
        adapter2.onParticipantsBtnClickListener = object:ChatMenuParticipantsAdapter.OnParticipantsBtnClickListener{
            override fun onAddClick(position: Int, uid: Long?) {
                //Toast.makeText(this@ChatActivity, position.toString(), Toast.LENGTH_SHORT).show()
                if(uid != null){
                    RetrofitClient.friendService.registerFriend("Bearer $accessToken", FriendUpdateDto(uid))
                        .enqueue(object: Callback<Unit>{
                            override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                                when(response.code()){
                                    200->{
                                        //파이어베이스에도 반영
                                        //updateFriendFirebase(uid)
                                        Log.i("밥친구 등록 ", "성공")
                                    }
                                    else->{
                                        Log.i("밥친구 등록 ", "실패")
                                    }
                                }
                            }

                            override fun onFailure(call: Call<Unit>, t: Throwable) {
                                TODO("Not yet implemented")
                            }

                        })
                }
            }

            override fun onBlockClick(position: Int, uid: Long?) {
                if(uid != null){
                    RetrofitClient.friendService.blockFriend("Bearer $accessToken", FriendUpdateDto(uid))
                        .enqueue(object: Callback<Unit>{
                            override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                                when(response.code()){
                                    200->{
                                        //파이어베이스에도 반영
                                        //updateBlockFirebase(uid)
                                        Log.i("차단  ", "성공")
                                    }
                                    else->{
                                        Log.i("차단 ", "실패")
                                    }
                                }
                            }

                            override fun onFailure(call: Call<Unit>, t: Throwable) {
                                TODO("Not yet implemented")
                            }
                        })
                }
            }
        }
        binding.menuRecyclerview.adapter = adapter2
    }

//    private fun updateFriendFirebase(friendUid: Long){
//        rf.child("participants/$friendUid/friendOrBlock").setValue("friend")
//    }

//    private fun updateBlockFirebase(blockUid: Long){
//        rf.child("participants/$blockUid/friendOrBlock").setValue("block")
//    }
}





