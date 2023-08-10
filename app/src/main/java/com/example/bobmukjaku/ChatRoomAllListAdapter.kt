package com.example.bobmukjaku

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.bobmukjaku.Model.ChatRoom
import com.example.bobmukjaku.databinding.ChatroomListBinding
import java.time.LocalDate
import java.time.format.DateTimeFormatter

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

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.name.text = items[position].roomName
        holder.binding.startTime.text = items[position].startTime
        holder.binding.endTime.text = items[position].endTime
        holder.binding.presentPerson.text = items[position].currentNum.toString()
        holder.binding.totalPerson.text = items[position].total.toString()

        when (items[position].kindOfFood) {
            "한식" -> {
                holder.binding.foodKind.text = "한식"
                val color = ContextCompat.getColor(holder.itemView.context, R.color.kor)
                holder.binding.foodKind.setBackgroundColor(color)
            }
            "일식" -> {
                holder.binding.foodKind.text = "일식"
                val color = ContextCompat.getColor(holder.itemView.context, R.color.jap)
                holder.binding.foodKind.setBackgroundColor(color)
            }
            "양식" -> {
                holder.binding.foodKind.text = "양식"
                val color = ContextCompat.getColor(holder.itemView.context, R.color.frg)
                holder.binding.foodKind.setBackgroundColor(color)
            }
            "중식" -> {
                holder.binding.foodKind.text = "중식"
                val color = ContextCompat.getColor(holder.itemView.context, R.color.chi)
                holder.binding.foodKind.setBackgroundColor(color)
            }
            "기타" -> {
                holder.binding.foodKind.text = "기타"
                val color = ContextCompat.getColor(holder.itemView.context, R.color.ect)
                holder.binding.foodKind.setBackgroundColor(color)
            }
        }

        val meetingDate = LocalDate.parse(items[position].meetingDate)

        if (meetingDate == LocalDate.now()) {
            holder.binding.date.text = "오늘"
        } else if (meetingDate == LocalDate.now().plusDays(1)) {
            holder.binding.date.text = "내일"
        } else {
            val formattedDate = meetingDate.format(DateTimeFormatter.ofPattern("MM/dd"))
            holder.binding.date.text = formattedDate
        }

        holder.binding.root.setOnClickListener {
            val roomInfo = items[position]
            if (roomInfo != null) {
                onItemClickListener?.onItemClick(position, roomInfo)
            }
        }
    }
}