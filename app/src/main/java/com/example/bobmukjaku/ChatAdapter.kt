package com.example.bobmukjaku

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.bobmukjaku.Model.ChatModel
import com.example.bobmukjaku.databinding.ChatMessageBinding

class ChatAdapter(var items:ArrayList<ChatModel>): RecyclerView.Adapter<ChatAdapter.ViewHolder>() {
    inner class ViewHolder(var binding:ChatMessageBinding):RecyclerView.ViewHolder(binding.root){
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = ChatMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.chat.text = items[position].message
    }
}