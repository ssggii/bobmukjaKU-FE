package com.bobmukja.bobmukjaku

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bobmukja.bobmukjaku.Model.ChatRoom
import com.bobmukja.bobmukjaku.Model.WrapperInChatRoomMenu
import com.bobmukja.bobmukjaku.databinding.ChatMenuRoomInfoBinding
import com.bobmukja.bobmukjaku.databinding.ChatMenuRoomParticipantsListBinding
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class ChatMenuParticipantsAdapter(var participants: ArrayList<WrapperInChatRoomMenu>, val context: Context, var chatRoomInfo: ChatRoom): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val rf = Firebase.database.getReference("chatRoom/${chatRoomInfo.roomId}/notice")
    inner class MenuInfoViewHolder(itemView: ChatMenuRoomInfoBinding)
        : RecyclerView.ViewHolder(itemView.root){
        private val chatRoomDateView = itemView.chatRoomDate
        private val chatRoomTimeView = itemView.chatRoomTime
        private val foodTypeView = itemView.foodType
        private val totalNumView = itemView.totalNum
        private val realStartTime = itemView.realStarttime
        private val realPlace = itemView.realPlace
        val timeFormat = "%02d:%02d"
        fun bind(position: Int){
            chatRoomDateView.text = chatRoomInfo.meetingDate
            val startHour = chatRoomInfo.startTime?.substring(0,2)?:"0"
            val startMinute = chatRoomInfo.startTime?.substring(3,5)?:"0"
            val endHour = chatRoomInfo.endTime?.substring(0,2)?:"0"
            val endMinute = chatRoomInfo.endTime?.substring(3,5)?:"0"
            var startTime = ""
            var endTime = ""
            if(startHour.toInt() > 11){
                startTime += "오후 "
                startTime += timeFormat.format(startHour.toInt() - 12, startMinute.toInt())
            }else{
                startTime += "오전 "
                startTime += timeFormat.format(startHour.toInt(), startMinute.toInt())
            }

            if(endHour.toInt() > 11){
                endTime += "오후 "
                endTime += timeFormat.format(endHour.toInt() - 12, endMinute.toInt())
            }else{
                endTime += "오전 "
                endTime += timeFormat.format(endHour.toInt(), endMinute.toInt())
            }
            chatRoomTimeView.text = "${startTime}~${endTime}"
            foodTypeView.text = chatRoomInfo.kindOfFood
            totalNumView.text = chatRoomInfo.total.toString()

            rf.get().addOnSuccessListener {
                if(it.value != null){
                    realStartTime.text = it.child("starttime").value.toString()
                    realPlace.text = it.child("restaurantName").value.toString()
                }else{
                    realStartTime.text = "-"
                    realPlace.text = "-"
                }
            }.addOnFailureListener {
                realStartTime.text = "-"
                realPlace.text = "-"
            }
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
        private val participantProfileBgView = itemView.participantProfileBg
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

            val bgResourceId = when (participants[position].member.profileColor) {
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
            participantProfileBgView.setImageResource(bgResourceId)

            var rate = participants[position].member.rate?:45
            var level = ""
            if (rate <= 20) {
                level = "1"
                participantProfileView.setBackgroundResource(R.drawable.ku_1)
            } else if (rate <= 40) {
                rate -= 20
                level = "2"
                participantProfileView.setBackgroundResource(R.drawable.ku_2)
            } else if (rate <= 60) {
                rate -= 40
                level = "3"
                participantProfileView.setBackgroundResource(R.drawable.ku_3)
            } else if (rate <= 80) {
                rate -= 60
                level = "4"
                participantProfileView.setBackgroundResource(R.drawable.ku_4)
            } else {
                rate -= 80
                level = "5"
                participantProfileView.setBackgroundResource(R.drawable.ku_5)
            }
            participantLevelView.text = level


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