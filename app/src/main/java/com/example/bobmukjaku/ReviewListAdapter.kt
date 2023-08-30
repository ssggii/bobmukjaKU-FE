package com.example.bobmukjaku

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.bobmukjaku.Model.RetrofitClient
import com.example.bobmukjaku.Model.ReviewResponse
import com.example.bobmukjaku.Model.SharedPreferences
import com.example.bobmukjaku.databinding.ReviewListBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ReviewListAdapter(var items: List<ReviewResponse>): RecyclerView.Adapter<ReviewListAdapter.ViewHolder>() {

    private val restaurantService = RetrofitClient.restaurantService
    private val accessToken = SharedPreferences.getString("accessToken", "")
    private val authorizationHeader = "Bearer $accessToken"

    interface OnItemClickListener{
        fun onItemClick(pos: Int, reviewInfo: ReviewResponse)
    }

    var onItemClickListener:OnItemClickListener? = null

    inner class ViewHolder(var binding: ReviewListBinding): RecyclerView.ViewHolder(binding.root){
        init{
            binding.root.setOnClickListener {
            }
        }
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
        holder.binding.name.text = reviewInfo.contents

        holder.binding.deleteBtn.setOnClickListener {
            deleteReview(reviewInfo)
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    private fun deleteReview(reviewInfo: ReviewResponse) {
        val call = restaurantService.deleteReview(authorizationHeader, uid = reviewInfo.uid, placeId = reviewInfo.placeId)
        call.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
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