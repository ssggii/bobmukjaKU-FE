package com.example.bobmukjaku

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.bobmukjaku.Model.ChatRoom
import com.example.bobmukjaku.databinding.ChatroomListBinding

class ChatRoomAllListAdapter(var items: List<ChatRoom>): RecyclerView.Adapter<ChatRoomAllListAdapter.ViewHolder>() {

    interface OnItemClickListener{
        fun onItemClick(pos: Int, roomInfo: ChatRoom)
    }

    var onItemClickListener:OnItemClickListener? = null

    inner class ViewHolder(var binding: ChatroomListBinding): RecyclerView.ViewHolder(binding.root){
        init{
            binding.root.setOnClickListener {
                val position = adapterPosition
                val noChatRoomsTextView = binding.noChatRoomsTextView
                val topContentView = binding.topContent
                val bottomContentView = binding.bottomContent

                if (position != RecyclerView.NO_POSITION) {
//                    val roomId = items[position].roomId
                    val roomInfo = items[position]
                    if (roomInfo != null) {
                        onItemClickListener?.onItemClick(position, roomInfo)
                        noChatRoomsTextView.visibility = View.GONE
                        topContentView.visibility = View.VISIBLE
                        bottomContentView.visibility = View.VISIBLE
                    } else {
                        noChatRoomsTextView.visibility = View.VISIBLE
                        topContentView.visibility = View.GONE
                        bottomContentView.visibility = View.GONE
                    }
                }
            }
        }
    }

    fun updateItems(newItems: List<ChatRoom>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = ChatroomListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.name.text = items[position].roomName
        holder.binding.startTime.text = items[position].startTime
        holder.binding.endTime.text = items[position].endTime
        holder.binding.presentPerson.text = items[position].currentNum.toString()
        holder.binding.totalPerson.text = items[position].total.toString()

        holder.binding.root.setOnClickListener {
            val roomInfo = items[position]
            if (roomInfo != null) {
                onItemClickListener?.onItemClick(position, roomInfo)
            }
        }
    }
}