package com.bobmukja.bobmukjaku

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bobmukja.bobmukjaku.Model.RetrofitClient
import com.bobmukja.bobmukjaku.Model.ReviewResponse
import com.bobmukja.bobmukjaku.Model.ScrapInfo
import com.bobmukja.bobmukjaku.Model.SharedPreferences
import com.bobmukja.bobmukjaku.databinding.ReviewListBinding
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException

class ReviewListAdapter(var items: List<ReviewResponse>, var uid: Long, var onReviewRemovedListener: OnReviewRemovedListener): RecyclerView.Adapter<ReviewListAdapter.ViewHolder>() {

    private val restaurantService = RetrofitClient.restaurantService
    private val accessToken = SharedPreferences.getString("accessToken", "")
    private val authorizationHeader = "Bearer $accessToken"

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
        holder.binding.content.text = "→ " + reviewInfo.contents

        // Firebase Storage에서 이미지 다운로드
        val imagePath = reviewInfo.imageUrl
        CoroutineScope(Dispatchers.IO).launch {
            val bitmap = downloadImageFromFirebaseStorage(imagePath)
            withContext(Dispatchers.Main) {
                if (bitmap != null) {
                    holder.binding.reviewImage.setImageBitmap(bitmap)
                } else {
                    // 이미지 다운로드 실패 처리
                    Log.i("리뷰 이미지 로드", "실패")
                }
            }
        }

        holder.binding.deleteBtn.setOnClickListener {
            deleteReview(reviewInfo, position)
        }
    }

    private suspend fun downloadImageFromFirebaseStorage(imagePath: String): Bitmap? {
        val storageReference = Firebase.storage.reference.child(imagePath)
        return try {
            val maxBufferSize = 10 * 1024 * 1024 // 최대 허용 버퍼 크기를 설정 (10MB로 설정)
            val bytes = storageReference.getBytes(maxBufferSize.toLong()).await()
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e("ImageDownload", "Image download failed: ${e.message}")
            null
        }
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