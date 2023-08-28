package com.example.bobmukjaku

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.bobmukjaku.Model.RestaurantList
import com.example.bobmukjaku.Model.ScrapInfo
import com.example.bobmukjaku.databinding.ScrapListBinding

class ScrapListAdapter(var items: List<ScrapInfo>, private val restaurantList: List<RestaurantList>): RecyclerView.Adapter<ScrapListAdapter.ViewHolder>() {

    interface OnItemClickListener{
        fun onItemClick(pos: Int, scrapInfo: ScrapInfo)
    }

    var onItemClickListener:OnItemClickListener? = null

    inner class ViewHolder(var binding: ScrapListBinding): RecyclerView.ViewHolder(binding.root){
        init{
            binding.root.setOnClickListener {
            }
        }
    }

    fun updateItems(newItems: List<ScrapInfo>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = ScrapListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val scrapInfo = items[position]
//        Log.i("res", restaurantList.toString())
//        for (lists in restaurantList) {
//            if (lists.bizesId == scrapInfo.placeId) {
//                holder.binding.name.text = lists.bizesNm
//            }
//        }

        holder.binding.name.text = scrapInfo.placeId
    }

    override fun getItemCount(): Int {
        return items.size
    }
}