package com.example.bobmukjaku

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bobmukjaku.Dto.FriendInfoDto
import com.example.bobmukjaku.Model.*
import com.example.bobmukjaku.databinding.FriendListBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FriendListAdapter(var items: List<FriendInfoDto>, var uid: Long, var onFriendRemovedListener: OnFriendRemovedListener): RecyclerView.Adapter<FriendListAdapter.ViewHolder>() {

    private val accessToken = SharedPreferences.getString("accessToken", "")
    private val authorizationHeader = "Bearer $accessToken"

    interface OnItemClickListener{
        fun onItemClick(pos: Int, scrapInfo: ScrapPost)
    }

    var onItemClickListener:OnItemClickListener? = null

    interface OnFriendRemovedListener {
        fun onFriendRemoved(position: Int)
    }

    inner class ViewHolder(var binding: FriendListBinding): RecyclerView.ViewHolder(binding.root){
    }

    fun updateItems(newItems: List<FriendInfoDto>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = FriendListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val friendInfo = items[position]

//        holder.binding.name.text = scrapInfo.placeName
//
//        // 리뷰 목록 API
//        val call = RetrofitClient.restaurantService.getRestaurantReview(authorizationHeader, scrapInfo.placeId)
//        call.enqueue(object : Callback<List<ReviewResponse>> {
//            override fun onResponse(call: Call<List<ReviewResponse>>, response: Response<List<ReviewResponse>>) {
//                if (response.isSuccessful) {
//                    val reviewListResponse = response.body() // 서버에서 받은 리뷰 목록
//                    if (reviewListResponse != null) {
//                        reviewList.clear()
//                        reviewList.addAll(reviewListResponse) // reviewList에 업데이트된 리뷰 목록 저장
//                        reviewAdapter.updateItems(reviewList) // 어댑터에 업데이트된 목록 전달
//
//                        holder.binding.totalReview.text = reviewList.size.toString()
//                    }
//                    val successCode = response.code()
//                    Toast.makeText(holder.binding.root.context, "음식점 리뷰 목록 로드. 성공 $successCode $scrapInfo.placeId", Toast.LENGTH_SHORT).show()
//                } else {
//                    val errorCode = response.code()
//                    Toast.makeText(holder.binding.root.context, "음식점 리뷰 목록 로드 실패. 에러 $errorCode", Toast.LENGTH_SHORT).show()
//                }
//            }
//
//            override fun onFailure(call: Call<List<ReviewResponse>>, t: Throwable) {
//                // 네트워크 오류 또는 기타 에러가 발생했을 때의 처리
//                t.message?.let { it1 -> Log.i("[음식점 리뷰 목록 로드 에러: ]", it1) }
//            }
//        })
//
//        holder.binding.reviewList.layoutManager = LinearLayoutManager(holder.binding.root.context, LinearLayoutManager.VERTICAL, false)
//        reviewAdapter = ScrapReviewListAdapter(reviewList)
//        reviewAdapter.onItemClickListener = object : ScrapReviewListAdapter.OnItemClickListener {
//            override fun onItemClick(pos: Int, reviewInfo: ReviewResponse) {
//            }
//        }
//        holder.binding.reviewList.adapter = reviewAdapter
//
//        // 스크랩 버튼 이벤트
//        holder.binding.scrapBtn.setOnClickListener {
//            val scrapInfo = ScrapInfo(uid = uid, placeId = items[position].placeId)
//            RetrofitClient.restaurantService.deleteScrap(authorizationHeader, scrapInfo).enqueue(object :
//                Callback<Void> {
//                override fun onResponse(call: Call<Void>, response: Response<Void>) {
//                    if (response.isSuccessful) {
//                        // 성공적으로 스크랩 해제 완료
//                        holder.binding.scrapBtn.backgroundTintList = ContextCompat.getColorStateList(holder.binding.root.context, R.color.main)
//                        Toast.makeText(holder.binding.root.context, "스크랩 해제가 완료되었습니다.", Toast.LENGTH_SHORT).show()
//
//                        // 스크랩 해제한 아이템의 위치를 리스너를 통해 알림
//                        onScrapRemovedListener.onScrapRemoved(position)
//                    } else {
//                        val errorCode = response.code()
//                        Toast.makeText(
//                            holder.binding.root.context,
//                            "스크랩 해제에 실패했습니다. 에러 코드: $errorCode",
//                            Toast.LENGTH_SHORT
//                        ).show()
//                    }
//                }
//
//                override fun onFailure(call: Call<Void>, t: Throwable) {
//                    // 네트워크 오류 또는 기타 에러가 발생했을 때의 처리
//                    t.message?.let { Log.i("[스크랩 해제 실패: ]", it) }
//                }
//            })
//        }
    }

    override fun getItemCount(): Int {
        return items.size
    }
}