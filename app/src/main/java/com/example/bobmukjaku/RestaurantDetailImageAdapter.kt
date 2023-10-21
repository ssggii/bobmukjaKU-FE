package com.example.bobmukjaku

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.bobmukjaku.Model.ReviewResponse
import com.example.bobmukjaku.databinding.ReviewImgListBinding
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.IOException

class RestaurantDetailImageAdapter(var items: List<ReviewResponse>) : RecyclerView.Adapter<RestaurantDetailImageAdapter.ViewHolder>() {

    interface OnItemClickListener{
        fun onItemClick(pos: Int, reviewInfo: ReviewResponse)
    }

    var onItemClickListener:OnItemClickListener? = null

    inner class ViewHolder(var binding: ReviewImgListBinding): RecyclerView.ViewHolder(binding.root){
    }

    fun updateItems(newItems: List<ReviewResponse>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = ReviewImgListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val reviewInfo = items[position]

        holder.itemView.setOnClickListener {
            onItemClickListener?.onItemClick(position, reviewInfo)
        }

        if (items.isEmpty()) {
            holder.binding.image.visibility = View.GONE
        }

        if (position < 3) {
            // 상위 3개 항목에 대해서만 리뷰 이미지 출력
            setImageToImageView(holder.binding.image, reviewInfo)
        } else {
            holder.binding.image.visibility = View.GONE
        }
    }

    private fun setImageToImageView(imageView: ImageView, reviewInfo: ReviewResponse) {
        val imagePath = reviewInfo.imageUrl
        CoroutineScope(Dispatchers.IO).launch {
            val bitmap = downloadImageFromFirebaseStorage(imagePath)
            withContext(Dispatchers.Main) {
                if (bitmap != null) {
                    imageView.setImageBitmap(bitmap)
                } else {
                    // 이미지 다운로드 실패 처리
                    Log.i("리뷰 이미지 로드", "실패")
                }
            }
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
}