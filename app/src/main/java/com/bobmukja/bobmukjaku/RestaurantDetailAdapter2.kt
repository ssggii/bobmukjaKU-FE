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
import com.bobmukja.bobmukjaku.databinding.ReviewDetailListBinding
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.IOException

class RestaurantDetailAdapter2(var items: List<ReviewResponse>) : RecyclerView.Adapter<RestaurantDetailAdapter2.ViewHolder>(){

    interface OnItemClickListener{
        fun onItemClick(pos: Int, reviewInfo: ReviewResponse)
    }

    var onItemClickListener:OnItemClickListener? = null

    inner class ViewHolder(var binding: ReviewDetailListBinding): RecyclerView.ViewHolder(binding.root){
    }

    fun updateItems(newItems: List<ReviewResponse>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = ReviewDetailListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val reviewInfo = items[position]

        if (items.isEmpty()) {
            holder.binding.content.visibility = View.GONE
            holder.binding.image.visibility = View.GONE
        }

        holder.binding.content.text = reviewInfo.contents
        if (reviewInfo.imageUrl != "nodata") {
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
            val maxBufferSize = 10 * 1024 * 1024 // 10MB로 설정
            val stream = storageReference.getBytes(maxBufferSize.toLong()).await().inputStream()

            // 이미지 리사이징
            val options = BitmapFactory.Options()
            options.inSampleSize = 3 // 이미지 크기를 줄임 (원하는 크기에 따라 조절)

            BitmapFactory.decodeStream(stream, null, options)
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