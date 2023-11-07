package com.bobmukja.bobmukjaku

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.bobmukja.bobmukjaku.Model.*
import com.bobmukja.bobmukjaku.databinding.FragmentMapScrapBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MapScrapFragment : Fragment(), ScrapListAdapter.OnScrapRemovedListener {

    lateinit var mContext: Context
    lateinit var binding: FragmentMapScrapBinding
    lateinit var adapter: ScrapListAdapter
    var scrapList = mutableListOf<ScrapPost>()
    var uid: Long = 0

    private val restaurantService = RetrofitClient.restaurantService
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
        binding = FragmentMapScrapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getUid()
    }

    private fun getScrapList() {
        val call = restaurantService.getMyScrap(authorizationHeader, uid)
        call.enqueue(object : Callback<List<ScrapPost>> {
            override fun onResponse(call: Call<List<ScrapPost>>, response: Response<List<ScrapPost>>) {
                if (response.isSuccessful) {
                    val scrapListResponse = response.body() // 서버에서 받은 스크랩 목록
                    if (scrapListResponse != null) {
                        scrapList.clear()
                        scrapList.addAll(scrapListResponse) // scrapList에 업데이트된 스크랩 목록 저장
                        adapter.updateItems(scrapList) // 어댑터에 업데이트된 목록 전달

                        binding.totalScrap.text = scrapList.size.toString()
                    } else {
                        scrapList.clear()
                        adapter.updateItems(scrapList)
                    }
                    val successCode = response.code()
                    Log.i("내 스크랩 목록 로드 ", "성공 $successCode")
                } else {
                    val errorCode = response.code()
                    Log.i("내 스크랩 목록 로드 ", "실패 $errorCode")
                }
            }

            override fun onFailure(call: Call<List<ScrapPost>>, t: Throwable) {
                // 네트워크 오류 또는 기타 에러가 발생했을 때의 처리
                t.message?.let { it1 -> Log.i("[내 스크랩 목록 로드 에러: ]", it1) }
            }
        })

        binding.myRecyclerView.layoutManager = LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false)
        adapter = ScrapListAdapter(scrapList, uid, this@MapScrapFragment)
        adapter.onItemClickListener = object : ScrapListAdapter.OnItemClickListener {
            override fun onItemClick(pos: Int, scrapInfo: ScrapPost) {
            }
        }
//        adapter.onScrapRemovedListener = object  : ScrapListAdapter.OnScrapRemovedListener {
//            override fun onScrapRemoved(position: Int) {
//                // 스크랩이 해제되었을 때 해당 아이템을 목록에서 제거하고 어댑터에 반영
//                scrapList.removeAt(position)
//                adapter.notifyDataSetChanged()
//            }
//        }
        binding.myRecyclerView.adapter = adapter
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
                        getScrapList()
                    }
                } else {
                    val errorCode = response.code()
                    Log.i("uid 가져오기 ", "실패 $errorCode")
                }
            }

            override fun onFailure(call: Call<Member>, t: Throwable) {
                // 네트워크 오류 또는 기타 에러가 발생했을 때의 처리
                t.message?.let { it1 -> Log.i("[uid 로드 실패: ]", it1) }
            }
        })
    }

    override fun onScrapRemoved(position: Int) {
        scrapList.removeAt(position)
        binding.totalScrap.text = scrapList.size.toString()
        adapter.notifyDataSetChanged()
    }
}