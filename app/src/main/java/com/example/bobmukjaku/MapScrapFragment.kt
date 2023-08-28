package com.example.bobmukjaku

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bobmukjaku.Model.*
import com.example.bobmukjaku.databinding.FragmentMapScrapBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MapScrapFragment : Fragment() {

    lateinit var mContext: Context
    lateinit var binding: FragmentMapScrapBinding
    lateinit var adapter: ScrapListAdapter
    private lateinit var viewModel: MapListViewModel
    var scrapList = mutableListOf<ScrapInfo>()
    var uid: Long = 0

    private val restaurantService = RetrofitClient.restaurantService
    private val accessToken = SharedPreferences.getString("accessToken", "")
    private val authorizationHeader = "Bearer $accessToken"

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
        val repository = RestaurantRepository()
        val viewModelFactory = MapListViewModelFactory(repository)
        viewModel = ViewModelProvider(this, viewModelFactory)[MapListViewModel::class.java]
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
        val indsMclsCdList = listOf("I201", "I202", "I203", "I204", "I205", "I206", "I211")
        val restaurantList2 = viewModel.restaurantList.value ?: emptyList()
//        for (categoryList in indsMclsCdList) {
//            viewModel.fetchRestaurantList(categoryList)
//            val restaurantList = viewModel.restaurantList.value ?: emptyList()
//        }

        val call = restaurantService.getMyScrap(authorizationHeader, uid)
        call.enqueue(object : Callback<List<ScrapInfo>> {
            override fun onResponse(call: Call<List<ScrapInfo>>, response: Response<List<ScrapInfo>>) {
                if (response.isSuccessful) {
                    val scrapListResponse = response.body() // 서버에서 받은 스크랩 목록
                    if (scrapListResponse != null) {
                        scrapList.clear()
                        scrapList.addAll(scrapListResponse) // scrapList에 업데이트된 스크랩 목록 저장
                        adapter.updateItems(scrapList) // 어댑터에 업데이트된 목록 전달

                        binding.totalScrap.text = scrapList.size.toString()
                    }
                    val successCode = response.code()
                    Toast.makeText(requireContext(), "내 스크랩 목록 로드. 성공 $successCode $uid", Toast.LENGTH_SHORT).show()
                } else {
                    val errorCode = response.code()
                    Toast.makeText(requireContext(), "내 스크랩 목록 로드 실패. 에러 $errorCode", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<ScrapInfo>>, t: Throwable) {
                // 네트워크 오류 또는 기타 에러가 발생했을 때의 처리
                t.message?.let { it1 -> Log.i("[내 스크랩 목록 로드 에러: ]", it1) }
            }
        })

        binding.myRecyclerView.layoutManager = LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false)
        adapter = ScrapListAdapter(scrapList, restaurantList = restaurantList2)
        adapter.onItemClickListener = object : ScrapListAdapter.OnItemClickListener {
            override fun onItemClick(pos: Int, scrapInfo: ScrapInfo) {
            }
        }
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