package com.example.bobmukjaku

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.bobmukjaku.Model.ReviewResponse
import com.example.bobmukjaku.databinding.ReviewAllListBinding

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
//        holder.binding.image = reviewInfo.imageUrl
    }

    override fun getItemCount(): Int {
        return items.size
    }
}