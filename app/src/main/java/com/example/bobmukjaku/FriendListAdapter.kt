package com.example.bobmukjaku

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.bobmukjaku.Dto.FriendInfoDto
import com.example.bobmukjaku.Model.*
import com.example.bobmukjaku.databinding.FriendListBinding

class FriendListAdapter(var items: List<FriendInfoDto>, var onFriendRemovedListener: OnFriendRemovedListener): RecyclerView.Adapter<FriendListAdapter.ViewHolder>() {

    interface OnItemClickListener{
        fun onItemClick(pos: Int, friendInfo: FriendInfoDto)
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
        val bgResourceId = when (friendInfo.friendProfileColor) {
            "bg1" -> R.drawable.bg1
            "bg2" -> R.drawable.bg2
            "bg3" -> R.drawable.bg3
            "bg4" -> R.drawable.bg4
            "bg5" -> R.drawable.bg5
            "bg6" -> R.drawable.bg6
            "bg7" -> R.drawable.bg7
            "bg8" -> R.drawable.bg8
            "bg9" -> R.drawable.bg9
            "bg10" -> R.drawable.bg10
            "bg11" -> R.drawable.bg11
            "bg12" -> R.drawable.bg12
            "bg13" -> R.drawable.bg13
            "bg14" -> R.drawable.bg14
            "bg15" -> R.drawable.bg15
            "bg16" -> R.drawable.bg16
            "bg17" -> R.drawable.bg17
            "bg18" -> R.drawable.bg18
            // 다른 리소스에 대한 처리 추가
            else -> R.drawable.bg1 // 디폴트 리소스 ID 또는 오류 처리 리소스 사용
        }

        holder.binding.imgProfileBg.setBackgroundResource(bgResourceId)
        holder.binding.tvItemChattingName.text = friendInfo.friendNickname

        var level = friendInfo.friendRate.toString().toInt()
        if (level <= 20) {
            holder.binding.level.text = "1"
            holder.binding.imgProfile.setBackgroundResource(R.drawable.ku_1)
        } else if (level <= 40) {
            level -= 20
            holder.binding.level.text = "2"
            holder.binding.imgProfile.setBackgroundResource(R.drawable.ku_2)
        } else if (level <= 60) {
            level -= 40
            holder.binding.level.text = "3"
            holder.binding.imgProfile.setBackgroundResource(R.drawable.ku_3)
        } else if (level <= 80) {
            level -= 60
            holder.binding.level.text = "4"
            holder.binding.imgProfile.setBackgroundResource(R.drawable.ku_4)
        } else {
            level -= 80
            holder.binding.level.text = "5"
            holder.binding.imgProfile.setBackgroundResource(R.drawable.ku_5)
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }
}