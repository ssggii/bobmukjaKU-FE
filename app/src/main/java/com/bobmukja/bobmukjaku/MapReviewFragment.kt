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
import com.bobmukja.bobmukjaku.databinding.FragmentMapReviewBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MapReviewFragment : Fragment(), ReviewListAdapter.OnReviewRemovedListener {

    lateinit var mContext: Context
    lateinit var binding: FragmentMapReviewBinding
    lateinit var adapter: ReviewListAdapter
    var reviewList = mutableListOf<ReviewResponse>()
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
        binding = FragmentMapReviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getUid()
    }

    private fun getReviewList() {
        val call = restaurantService.getMyReview(authorizationHeader, uid)
        call.enqueue(object : Callback<List<ReviewResponse>> {
            override fun onResponse(call: Call<List<ReviewResponse>>, response: Response<List<ReviewResponse>>) {
                if (response.isSuccessful) {
                    val reviewResponse = response.body() // 서버에서 받은 리뷰 목록
                    if (reviewResponse != null) {
                        reviewList.clear()
                        reviewList.addAll(reviewResponse) // reviewList에 업데이트된 리뷰 목록 저장
                        adapter.updateItems(reviewList) // 어댑터에 업데이트된 목록 전달

                        binding.totalReview.text = reviewList.size.toString()
                    }
                    val successCode = response.code()
                    Toast.makeText(requireContext(), "내 리뷰 목록 로드. 성공 $successCode $uid", Toast.LENGTH_SHORT).show()
                } else {
                    val errorCode = response.code()
                    Toast.makeText(requireContext(), "내 리뷰 목록 로드 실패. 에러 $errorCode", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<ReviewResponse>>, t: Throwable) {
                // 네트워크 오류 또는 기타 에러가 발생했을 때의 처리
                t.message?.let { it1 -> Log.i("[내 리뷰 목록 로드 에러: ]", it1) }
            }
        })

        binding.myRecyclerView.layoutManager = LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false)
        adapter = ReviewListAdapter(reviewList, uid, this@MapReviewFragment)
        adapter.onItemClickListener = object : ReviewListAdapter.OnItemClickListener {
            override fun onItemClick(pos: Int, reviewInfo: ReviewResponse) {
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
                        getReviewList()
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

    override fun onReviewRemoved(position: Int) {
        reviewList.removeAt(position)
        binding.totalReview.text = reviewList.size.toString()
        adapter.notifyDataSetChanged()
    }
}