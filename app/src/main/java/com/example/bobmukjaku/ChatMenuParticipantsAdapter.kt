package com.example.bobmukjaku

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.bobmukjaku.Model.ChatRoom
import com.example.bobmukjaku.Model.WrapperInChatRoomMenu
import com.example.bobmukjaku.databinding.ChatMenuRoomInfoBinding
import com.example.bobmukjaku.databinding.ChatMenuRoomParticipantsListBinding

class ChatMenuParticipantsAdapter(var participants: ArrayList<WrapperInChatRoomMenu>, val context: Context, var chatRoomInfo: ChatRoom): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    inner class MenuInfoViewHolder(itemView: ChatMenuRoomInfoBinding)
        : RecyclerView.ViewHolder(itemView.root){
        private val chatRoomDateView = itemView.chatRoomDate
        private val chatRoomTimeView = itemView.chatRoomTime
        private val foodTypeView = itemView.foodType
        private val totalNumView = itemView.totalNum
        private val realStartTime = itemView.realStarttime
        private val realPlace = itemView.realPlace
        fun bind(position: Int){
            chatRoomDateView.text = chatRoomInfo.meetingDate
            chatRoomTimeView.text = "${chatRoomInfo.startTime}~${chatRoomInfo.endTime}"
            foodTypeView.text = chatRoomInfo.kindOfFood
            totalNumView.text = chatRoomInfo.total.toString()
            realStartTime.text = chatRoomInfo.startTime
            realPlace.text = "???"
        }
    }

    interface OnParticipantsBtnClickListener{
        fun onAddClick(position: Int, uid: Long?)
        fun onBlockClick(position: Int, uid: Long?)
    }
    var onParticipantsBtnClickListener: OnParticipantsBtnClickListener? = null
    inner class ParticipantsListViewHolder(itemView: ChatMenuRoomParticipantsListBinding)
        : RecyclerView.ViewHolder(itemView.root){
        private val participantNameView = itemView.participantName
        private val participantLevelView = itemView.participantLevel
        private val participantProfileView = itemView.participantProfile
        private val addBtnView = itemView.addBtn
        private val blockBtnView = itemView.blockBtn
        private val friendOrBlockView = itemView.friendOrBlock
        init {
            addBtnView.setOnClickListener{
                onParticipantsBtnClickListener?.onAddClick(adapterPosition, participants[adapterPosition].member.uid)
                addBtnView.visibility = View.GONE
                blockBtnView.visibility = View.GONE
                friendOrBlockView.visibility = View.VISIBLE
                friendOrBlockView.text = "친구"
            }
            blockBtnView.setOnClickListener {
                onParticipantsBtnClickListener?.onBlockClick(adapterPosition, participants[adapterPosition].member.uid)
                addBtnView.visibility = View.GONE
                blockBtnView.visibility = View.GONE
                friendOrBlockView.visibility = View.VISIBLE
                friendOrBlockView.text = "차단함"
            }
        }
        fun bind(position: Int){
            participantNameView.text = participants[position].member.memberNickName
            participantLevelView.text = participants[position].member.rate.toString()
            participantProfileView.background = context.getDrawable(
                context.resources.getIdentifier("bg1","drawable", context.packageName))

            when(participants[position].friendOrBlock){
                "friend"->{
                    addBtnView.visibility = View.GONE
                    blockBtnView.visibility = View.GONE
                    friendOrBlockView.visibility = View.VISIBLE
                    friendOrBlockView.text = "친구"
                }
                "block"->{
                    addBtnView.visibility = View.GONE
                    blockBtnView.visibility = View.GONE
                    friendOrBlockView.visibility = View.VISIBLE
                    friendOrBlockView.text = "차단함"
                }
                "na"->{
                    addBtnView.visibility = View.VISIBLE
                    blockBtnView.visibility = View.VISIBLE
                    friendOrBlockView.visibility = View.GONE
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType){
            1->{
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.chat_menu_room_info, parent, false)
                return MenuInfoViewHolder(ChatMenuRoomInfoBinding.bind(view))
            }
            else->{
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.chat_menu_room_participants_list, parent, false)
                return ParticipantsListViewHolder(ChatMenuRoomParticipantsListBinding.bind(view))
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return participants[position].menuType
    }

    override fun getItemCount(): Int {
        return participants.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(participants[position].menuType){
            1->{
                (holder as MenuInfoViewHolder).bind(position)
            }
            else->{
                (holder as ParticipantsListViewHolder).bind(position)
            }
        }
    }
}