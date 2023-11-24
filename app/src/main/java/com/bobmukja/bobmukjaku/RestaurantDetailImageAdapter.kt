package com.bobmukja.bobmukjaku

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bobmukja.bobmukjaku.Model.ReviewResponse
import com.bobmukja.bobmukjaku.databinding.ReviewImgListBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
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
            if (reviewInfo.imageUrl != "nodata") {
                setImageToImageView(holder.binding.image, reviewInfo)
            } else {
                holder.binding.image.visibility = View.GONE
            }
        } else {
            holder.binding.image.visibility = View.GONE
        }
    }

    private fun setImageToImageView(imageView: ImageView, reviewInfo: ReviewResponse) {
        val imagePath = reviewInfo.imageUrl
        CoroutineScope(Dispatchers.IO).launch {
            downloadImageAndSetToImageView(imageView, imagePath)
        }
    }

    private suspend fun downloadImageAndSetToImageView(imageView: ImageView, imagePath: String) {
        val storageReference = Firebase.storage.reference.child(imagePath)

        try {
            val maxBufferSize = 10 * 1024 * 1024 // 10MB로 설정
            val bytes = storageReference.getBytes(maxBufferSize.toLong()).await()

            // Glide를 사용하여 이미지 로드 및 설정
            withContext(Dispatchers.Main) {
                Glide.with(imageView.context)
                    .load(bytes)
                    .apply(RequestOptions().override(300, 300)) // 이미지 크기 조절 (원하는 크기에 따라 조절)
                    .into(imageView)
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e("ImageDownload", "Image download failed: ${e.message}")
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }
}