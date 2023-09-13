package com.example.bobmukjaku

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.bobmukjaku.Model.ChatModel
import com.example.bobmukjaku.Model.Member
import com.example.bobmukjaku.databinding.MessageListMineBinding
import com.example.bobmukjaku.databinding.MessageListOthersBinding
import java.util.*

class ChatAdapter(var items:ArrayList<ChatModel>, var myInfo: Member): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            1 -> {            //메시지가 내 메시지인 경우
                val view =
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.message_list_mine, parent, false)   //내 메시지 레이아웃으로 초기화

                MyMessageViewHolder(MessageListMineBinding.bind(view))
            }
            else -> {      //메시지가 상대 메시지인 경우
                val view =
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.message_list_others, parent, false)  //상대 메시지 레이아웃으로 초기화
                OtherMessageViewHolder(MessageListOthersBinding.bind(view))
            }
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    // 여기 아래 부분 새로 추가 (실제 실행 시 반영 X) 07/31
    override fun getItemViewType(position: Int): Int {               //메시지의 id에 따라 내 메시지/상대 메시지 구분
        //return if (items[position].senderUid.equals(myUid)) 1 else 0
        return if (items[position].senderUid == myInfo.uid) 1 else 0
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
//        holder.binding.tvName.text = items[position].senderName
//        holder.binding.tvMessage.text = items[position].message

        if (items[position].senderUid == myInfo.uid) {       //레이아웃 항목 초기화
            (holder as MyMessageViewHolder).bind(position)
        } else {
            (holder as OtherMessageViewHolder).bind(position)
        }
    }

    inner class OtherMessageViewHolder(itemView: MessageListOthersBinding) :         //상대 메시지 뷰홀더
        RecyclerView.ViewHolder(itemView.root) {
        var background = itemView.background
        var txtMessage = itemView.tvMessage
        var txtName = itemView.tvName
        var txtDate = itemView.tvDate

        fun bind(position: Int) {           //메시지 UI 항목 초기화
            var message = items[position]
            var sendDate = message.time

            if(items[position].isProfanity){
                txtMessage.text = "욕설이 감지됐습니다."
            }else {
                txtMessage.text = message.message
            }
            txtName.text = message.senderName
            Log.i("kkkkk",sendDate.toString())
            txtDate.text = getTime(sendDate)

//            if (message.confirmed)           //확인 여부 표시
//                txtIsShown.visibility = View.GONE
//            else
//                txtIsShown.visibility = View.VISIBLE

            //setShown(position)             //해당 메시지 확인하여 서버로 전송
        }

        fun getDateText(sendDate: String): String {    //메시지 전송 시각 생성

            var dateText = ""
            var timeString = ""
            if (sendDate.isNotBlank()) {
                timeString = sendDate.substring(8, 12)
                var hour = timeString.substring(0, 2)
                var minute = timeString.substring(2, 4)

                var timeformat = "%02d:%02d"

                if (hour.toInt() > 11) {
                    dateText += "오후 "
                    dateText += timeformat.format(hour.toInt() - 12, minute.toInt())
                } else {
                    dateText += "오전 "
                    dateText += timeformat.format(hour.toInt(), minute.toInt())
                }
            }
            return dateText
        }

        fun setShown(position: Int) {          //메시지 확인하여 서버로 전송
//            firebaseDatabase
//                .child("chatRooms").child(chatRoomKey!!).child("messages")
//                .child(messageKeys[position]).child("confirmed").setValue(true)
//                .addOnSuccessListener {
//                    Log.i("checkShown", "성공")
//                }
        }

        fun getTime(milliseconds: Long?): String?{
            val calendar = Calendar.getInstance()
            if(milliseconds != null) {
                calendar.timeInMillis = milliseconds
                val hour = calendar[Calendar.HOUR_OF_DAY]
                val minutes = calendar[Calendar.MINUTE]
                when{
                    (hour > 12) -> {
                        return "오후 ".plus(hour.minus(12).toString().plus(":").plus(minutes.toString()))
                    }
                    else->{
                        return "오전 ".plus(hour.toString().plus(":").plus(minutes.toString()))
                    }
                }
            }else{
                return null
            }
        }
    }

    inner class MyMessageViewHolder(itemView: MessageListMineBinding) :       // 내 메시지용 ViewHolder
        RecyclerView.ViewHolder(itemView.root) {
        var background = itemView.background
        var txtMessage = itemView.txtMessage
        var txtDate = itemView.txtDate
        //var txtIsShown = itemView.txtIsShown

        fun bind(position: Int) {            //메시지 UI 레이아웃 초기화
            var message = items[position]
            var sendDate = message.time

            if(items[position].isProfanity){
                txtMessage.text = "욕설이 감지됐습니다."
            }else {
                txtMessage.text = message.message
            }

            //txtDate.text = getDateText(sendDate.toString())
            txtDate.text = getTime(sendDate)

//            if (message.confirmed)
//                txtIsShown.visibility = View.GONE
//            else
//                txtIsShown.visibility = View.VISIBLE
        }

        fun getDateText(sendDate: String): String {        //메시지 전송 시각 생성
            var dateText = ""
            var timeString = ""
            if (sendDate.isNotBlank()) {
                timeString = sendDate.substring(8, 12)
                var hour = timeString.substring(0, 2)
                var minute = timeString.substring(2, 4)

                var timeformat = "%02d:%02d"

                if (hour.toInt() > 11) {
                    dateText += "오후 "
                    dateText += timeformat.format(hour.toInt() - 12, minute.toInt())
                } else {
                    dateText += "오전 "
                    dateText += timeformat.format(hour.toInt(), minute.toInt())
                }
            }
            return dateText
        }

        fun getTime(milliseconds: Long?): String?{
            val calendar = Calendar.getInstance()
            if(milliseconds != null) {
                calendar.timeInMillis = milliseconds
                val hour = calendar[Calendar.HOUR_OF_DAY]
                val minutes = calendar[Calendar.MINUTE]
                when{
                    (hour > 12) -> {
                        return "오후 ".plus(hour.minus(12).toString().plus(":").plus(minutes.toString()))
                    }
                    else->{
                        return "오전 ".plus(hour.toString().plus(":").plus(minutes.toString()))
                    }
                }
            }else{
                return null
            }
        }
    }
}