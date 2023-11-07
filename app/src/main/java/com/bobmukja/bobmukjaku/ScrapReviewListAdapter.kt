package com.bobmukja.bobmukjaku

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bobmukja.bobmukjaku.Model.ReviewResponse
import com.bobmukja.bobmukjaku.databinding.ReviewAllListBinding
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

        if (items.isEmpty()) {
            holder.binding.content.visibility = View.GONE
        }

        if (position < 3) {
            // 상위 3개 항목에 대해서만 리뷰 출력
            holder.binding.content.text = "→ " + reviewInfo.contents
        } else {
            holder.binding.content.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }
}