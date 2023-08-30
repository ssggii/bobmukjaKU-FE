package com.example.bobmukjaku

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bobmukjaku.Model.RetrofitClient
import com.example.bobmukjaku.Model.ReviewResponse
import com.example.bobmukjaku.Model.ScrapInfo
import com.example.bobmukjaku.Model.SharedPreferences
import com.example.bobmukjaku.databinding.ReviewListBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ReviewListAdapter(var items: List<ReviewResponse>, var uid: Long, var onReviewRemovedListener: OnReviewRemovedListener): RecyclerView.Adapter<ReviewListAdapter.ViewHolder>() {

    lateinit var reviewAdapter: ScrapReviewListAdapter
    private val restaurantService = RetrofitClient.restaurantService
    private val accessToken = SharedPreferences.getString("accessToken", "")
    private val authorizationHeader = "Bearer $accessToken"

    var reviewList = mutableListOf<ReviewResponse>()

    interface OnItemClickListener{
        fun onItemClick(pos: Int, reviewInfo: ReviewResponse)
    }

    var onItemClickListener:OnItemClickListener? = null

    interface OnReviewRemovedListener {
        fun onReviewRemoved(position: Int)
    }

    inner class ViewHolder(var binding: ReviewListBinding): RecyclerView.ViewHolder(binding.root){
    }

    fun updateItems(newItems: List<ReviewResponse>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = ReviewListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val reviewInfo = items[position]
        holder.binding.name.text = reviewInfo.placeName

        holder.binding.deleteBtn.setOnClickListener {
            deleteReview(reviewInfo, position)
        }

        // 리뷰 목록 API
        val call = RetrofitClient.restaurantService.getRestaurantReview(authorizationHeader, reviewInfo.placeId)
        call.enqueue(object : Callback<List<ReviewResponse>> {
            override fun onResponse(call: Call<List<ReviewResponse>>, response: Response<List<ReviewResponse>>) {
                if (response.isSuccessful) {
                    val reviewListResponse = response.body() // 서버에서 받은 리뷰 목록
                    if (reviewListResponse != null) {
                        reviewList.clear()
                        reviewList.addAll(reviewListResponse) // reviewList에 업데이트된 리뷰 목록 저장
                        reviewAdapter.updateItems(reviewList) // 어댑터에 업데이트된 목록 전달

                        holder.binding.totalReview.text = reviewList.size.toString()
                    }
                    val successCode = response.code()
                    Toast.makeText(holder.binding.root.context, "음식점 리뷰 목록 로드. 성공 $successCode $reviewInfo.placeId", Toast.LENGTH_SHORT).show()
                } else {
                    val errorCode = response.code()
                    Toast.makeText(holder.binding.root.context, "음식점 리뷰 목록 로드 실패. 에러 $errorCode", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<ReviewResponse>>, t: Throwable) {
                // 네트워크 오류 또는 기타 에러가 발생했을 때의 처리
                t.message?.let { it1 -> Log.i("[음식점 리뷰 목록 로드 에러: ]", it1) }
            }
        })

        holder.binding.reviewList.layoutManager = LinearLayoutManager(holder.binding.root.context, LinearLayoutManager.VERTICAL, false)
        reviewAdapter = ScrapReviewListAdapter(reviewList)
        reviewAdapter.onItemClickListener = object : ScrapReviewListAdapter.OnItemClickListener {
            override fun onItemClick(pos: Int, reviewInfo: ReviewResponse) {
            }
        }
        holder.binding.reviewList.adapter = reviewAdapter
    }

    override fun getItemCount(): Int {
        return items.size
    }

    private fun deleteReview(reviewInfo: ReviewResponse, position: Int) {
        val review = ScrapInfo(uid = uid, placeId = reviewInfo.placeId)
        val call = restaurantService.deleteReview(authorizationHeader, review)
        call.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    onReviewRemovedListener.onReviewRemoved(position)
                    Log.i("deleteReview", "리뷰 삭제 완료")
                } else {
                    val errorCode = response.code()
                    Log.i("deleteReview", "리뷰 삭제 실패 $errorCode")
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                // 네트워크 오류 또는 기타 에러가 발생했을 때의 처리
                t.message?.let { it1 -> Log.i("[리뷰 삭제 기타 에러: ]", it1) }
            }
        })
    }
}