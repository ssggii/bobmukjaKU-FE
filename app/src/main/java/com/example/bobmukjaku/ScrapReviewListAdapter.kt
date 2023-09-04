package com.example.bobmukjaku

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.bobmukjaku.Model.ReviewResponse
import com.example.bobmukjaku.databinding.ReviewAllListBinding
import com.google.firebase.ktx.Firebase

class ScrapReviewListAdapter(var items: List<ReviewResponse>) : RecyclerView.Adapter<ScrapReviewListAdapter.ViewHolder>() {

    interface OnItemClickListener{
        fun onItemClick(pos: Int, reviewInfo: ReviewResponse)
    }

    var onItemClickListener:OnItemClickListener? = null

    inner class ViewHolder(var binding: ReviewAllListBinding): RecyclerView.ViewHolder(binding.root){
    }

    fun updateItems(newItems: List<ReviewResponse>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = ReviewAllListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val reviewInfo = items[position]

        holder.binding.content.text = "→ " + reviewInfo.contents

        // 파이어베이스 스토리지에서 이미지 다운로드 및 설정
//        if (reviewInfo.imageUrl.isNotEmpty()) {
//            val storageRef = Firebase.storage.reference.child(reviewInfo.imageUrl)
//
//            storageRef.downloadUrl.addOnSuccessListener { uri ->
//                Glide.with(holder.itemView.context)
//                    .load(uri)
//                    .into(holder.binding.image) // Glide를 사용하여 이미지 설정
//            }.addOnFailureListener {
//                // 이미지 다운로드 실패 시 처리
//                Log.e("ImageDownload", "Failed to download image: ${it.message}")
//            }
//        } else {
//            // 이미지 주소가 없을 경우 처리
//            holder.binding.image.setImageResource(R.drawable.default_image) // 기본 이미지 설정 또는 비어있는 경우 아무 작업하지 않음
//        }
    }

    override fun getItemCount(): Int {
        return items.size
    }
}