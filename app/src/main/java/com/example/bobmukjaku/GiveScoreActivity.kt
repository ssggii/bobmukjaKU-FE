package com.example.bobmukjaku

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bobmukjaku.Model.Member
import com.example.bobmukjaku.Model.RetrofitClient
import com.example.bobmukjaku.Model.SharedPreferences
import com.example.bobmukjaku.Model.UpdateScoreInfo
import com.example.bobmukjaku.databinding.ActivityGiveScoreBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class GiveScoreActivity : AppCompatActivity() {
    lateinit var binding: ActivityGiveScoreBinding
    var participants = arrayListOf<UpdateScoreInfo>()
    lateinit var adapter: GiveScoreAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGiveScoreBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initRecyclerView()
        initLayout()
    }

    private fun initRecyclerView() {
        //참가자목록을 담는 recyclerView 초기화

        //서버에서 방id로 현재 참가자 목록을 받아온다.
        /*---가정---*/
        participants.add(UpdateScoreInfo(Member(1,null, null, "aaa", null, 40, "bg1"), false, false))
        //participants.add(UpdateScoreInfo(Member(2,null, null, "밥먹자쿠2", null, 40, "bg1"), false, false))
        //participants.add(UpdateScoreInfo(Member(3,null, null, "밥먹자쿠3", null, 40, "bg1"), false, false))

        binding.participantsRecyclerview.apply {
            layoutManager = LinearLayoutManager(this@GiveScoreActivity, LinearLayoutManager.VERTICAL, false )
            adapter = GiveScoreAdapter(participants, context)
        }

    }

    private fun initLayout() {
        binding.nextBtn.setOnClickListener {
            val accessToken = SharedPreferences.getString("accessToken", "")?:""
            val memberService = RetrofitClient.memberService
            //해당 버튼을 누르면 평가점수가 업데이트 되며 리뷰화면으로 전환
            for(result in participants){
                val originalScore = result.participant.rate

                if(result.thumbUp){
                    val request = memberService.updateMember(accessToken, mapOf("rate" to originalScore?.plus(1).toString()))
                    request.enqueue(object: Callback<Void>{
                        override fun onResponse(call: Call<Void>, response: Response<Void>) {

                        }

                        override fun onFailure(call: Call<Void>, t: Throwable) {
                            TODO("Not yet implemented")
                        }

                    })
                }
                else if(result.thumbDown){
                    val request = memberService.updateMember(accessToken, mapOf("rate" to originalScore?.minus(1).toString()))
                    request.enqueue(object: Callback<Void>{
                        override fun onResponse(call: Call<Void>, response: Response<Void>) {

                        }

                        override fun onFailure(call: Call<Void>, t: Throwable) {
                            TODO("Not yet implemented")
                        }

                    })
                }
            }
        }
    }
}