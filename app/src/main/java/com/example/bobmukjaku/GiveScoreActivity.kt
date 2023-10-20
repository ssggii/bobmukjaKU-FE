package com.example.bobmukjaku

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bobmukjaku.Dto.RateUpdateDto
import com.example.bobmukjaku.Model.Member
import com.example.bobmukjaku.Model.RetrofitClient
import com.example.bobmukjaku.Model.SharedPreferences
import com.example.bobmukjaku.Model.UpdateScoreInfo
import com.example.bobmukjaku.databinding.ActivityGiveScoreBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class GiveScoreActivity : AppCompatActivity() {
    lateinit var binding: ActivityGiveScoreBinding
    var participants = arrayListOf<UpdateScoreInfo>()
    lateinit var adapter: GiveScoreAdapter
    lateinit var accessToken: String
    val memberService = RetrofitClient.memberService
    lateinit var myInfo: Member
    var roomId = 0L



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGiveScoreBinding.inflate(layoutInflater)
        setContentView(binding.root)

        try{
            roomId = intent.getLongExtra("roomId", -1)
            Log.i("roomroom", "$roomId")
        }catch (e: Exception){
            Log.i("errorMessage", "roomId문제 ${e.message}")
        }

        try {
            SharedPreferences.initSharedPreferences(applicationContext)
            accessToken = "Bearer ".plus(SharedPreferences.getString("accessToken", "") ?: "")
            Log.i("errorMessage", accessToken)
        }catch (e: java.lang.Exception){
            Log.i("errorMessage", "SharedPreference문제 ${e.message}")
        }


        val getMyInfoJob = CoroutineScope(Dispatchers.IO).async {
            getMyInfoFromServer()
        }

//        Log.i("errorMessage", roomId.toString())
        CoroutineScope(Dispatchers.Main).launch {
            myInfo = getMyInfoJob.await()
            val getAllParticipantsInRoomJob = CoroutineScope(Dispatchers.IO).async {
                getAllParticipants(roomId)
            }
            getAllParticipantsInRoomJob.await()
            initRecyclerView()
            initLayout()
        }

    }

    private fun getMyInfoFromServer(): Member{
        val accessToken = SharedPreferences.getString("accessToken", "")
        //서버에서 내정보 가져오기
        val request = RetrofitClient.memberService.selectOne(
            "Bearer $accessToken")
        val response = request.execute()
        return response.body()!!
    }
    private fun initRecyclerView() {
        //참가자목록을 담는 recyclerView 초기화



        binding.participantsRecyclerview.apply {
            layoutManager = LinearLayoutManager(this@GiveScoreActivity, LinearLayoutManager.VERTICAL, false )
            adapter = GiveScoreAdapter(participants, context)
        }

    }

    private fun getAllParticipants(roomId: Long) {

        //방의 참여자 목록을 서버에서 받아온다.
        val request = memberService.getParticipantsInRoom(accessToken, roomId)
        val response = request.execute()
        if(response.isSuccessful){
            val participantList = response.body()
            Log.i("참여자", participantList.toString())
            if(!participantList.isNullOrEmpty()){
                for(participant in participantList){
                    if(myInfo.uid == participant.uid)continue

                    participants.add(
                        UpdateScoreInfo(participant,
                            thumbUp = false,
                            thumbDown = false
                        )
                    )
                }
            }
        }
    }

    private fun initLayout() {
        binding.nextBtn.setOnClickListener {

            //해당 버튼을 누르면 평가점수가 업데이트 되며 리뷰화면으로 전환
            for(result in participants){
                val originalScore = result.participant.rate

                if(result.thumbUp){
                    val request = memberService.rateUpdate(accessToken, RateUpdateDto(result.participant.uid, 20))
                    request.enqueue(object: Callback<Void> {
                        override fun onResponse(call: Call<Void>, response: Response<Void>) {

                        }

                        override fun onFailure(call: Call<Void>, t: Throwable) {

                        }

                    })
                }
                else if(result.thumbDown){
                    val request = memberService.rateUpdate(accessToken, RateUpdateDto(result.participant.uid, -20))
                    request.enqueue(object: Callback<Void>{
                        override fun onResponse(call: Call<Void>, response: Response<Void>) {

                        }

                        override fun onFailure(call: Call<Void>, t: Throwable) {

                        }
                    })
                }
            }

            //리뷰 화면으로 이동
            val intent = Intent(this, ReviewActivity::class.java)
            intent.putExtra("myInfo", myInfo)
            intent.putExtra("roomId", roomId)
            startActivity(intent)
        }
    }
}