package com.example.bobmukjaku

import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.bobmukjaku.Model.ChatModel
import com.example.bobmukjaku.Model.Member
import com.example.bobmukjaku.databinding.MessageListMineBinding
import com.example.bobmukjaku.databinding.MessageListOthersBinding
import com.example.bobmukjaku.databinding.SharemessageMineBinding
import com.example.bobmukjaku.databinding.SharemessageOtherBinding
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
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
            2->{
                val view =
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.sharemessage_mine, parent, false)   //내 메시지 레이아웃으로 초기화

                MyShareMessageViewHolder(SharemessageMineBinding.bind(view))
            }
            3 -> {      //메시지가 상대 메시지인 경우
                val view =
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.message_list_others, parent, false)  //상대 메시지 레이아웃으로 초기화
                OtherMessageViewHolder(MessageListOthersBinding.bind(view))
            }
            else ->{
                //미완성
                val view =
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.sharemessage_other, parent, false)   //내 메시지 레이아웃으로 초기화

                OtherShareMessageViewHolder(SharemessageOtherBinding.bind(view))
            }
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    // 여기 아래 부분 새로 추가 (실제 실행 시 반영 X) 07/31
//    override fun getItemViewType(position: Int): Int {               //메시지의 id에 따라 내 메시지/상대 메시지 구분
//        //return if (items[position].senderUid.equals(myUid)) 1 else 0
//        return if (items[position].senderUid == myInfo.uid) 1 else 0
//    }

    override fun getItemViewType(position: Int): Int {               //메시지의 id에 따라 내 메시지/상대 메시지 구분
        //return if (items[position].senderUid.equals(myUid)) 1 else 0
        return if ((items[position].senderUid == myInfo.uid)&&(items[position].shareMessage == false)){
            1
        }else if((items[position].senderUid == myInfo.uid)&&(items[position].shareMessage == true)){
            2
        }else if((items[position].senderUid != myInfo.uid)&&(items[position].shareMessage == false)){
            3
        }else{
            4
        }
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
//        holder.binding.tvName.text = items[position].senderName
//        holder.binding.tvMessage.text = items[position].message

        if ((items[position].senderUid == myInfo.uid) && (items[position].shareMessage == false)) {       //레이아웃 항목 초기화
            (holder as MyMessageViewHolder).bind(position)
        }else if((items[position].senderUid == myInfo.uid) && (items[position].shareMessage == true)){
            (holder as MyShareMessageViewHolder).bind(position)
        }else if((items[position].senderUid != myInfo.uid) && (items[position].shareMessage == false)){
            (holder as OtherMessageViewHolder).bind(position)
        }else{
            (holder as OtherShareMessageViewHolder).bind(position)
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

            if(items[position].profanity){
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
    }

    fun getTime(milliseconds: Long?): String?{
        val calendar = Calendar.getInstance()
        if(milliseconds != null) {
            calendar.timeInMillis = milliseconds
            val hour = calendar[Calendar.HOUR_OF_DAY]
            val minutes = calendar[Calendar.MINUTE]
            when {
                (hour > 12) -> {
                    val hourString = if (hour < 10) "0$hour" else hour.toString()
                    val minutesString = if (minutes < 10) "0$minutes" else minutes.toString()
                    return "$hourString:$minutesString"
                }
                else -> {
                    val hourString = if (hour < 10) "0$hour" else hour.toString()
                    val minutesString = if (minutes < 10) "0$minutes" else minutes.toString()
                    return "$hourString:$minutesString"
                }
            }

        }else{
            return null
        }
    }

    inner class OtherShareMessageViewHolder(itemView: SharemessageOtherBinding) :
        RecyclerView.ViewHolder(itemView.root){

            var restaurantNameTxt = itemView.restaurantName
            var restaurantAddressTxt = itemView.restaurantAddress
            var reviewImg = itemView.reviewImg
            var timeTxt = itemView.txtDate
            var nameTxt = itemView.tvName

            fun bind(position: Int){
                var contentList = items[position].message!!.split("|")
                var restaurantName = contentList[0]
                var restaurantAddress = contentList[1]
                var imageUrl = contentList[2]


                nameTxt.text = items[position].senderName
                restaurantNameTxt.text = restaurantName
                restaurantAddressTxt.text = restaurantAddress
                timeTxt.text = getTime(items[position].time)
                val rf = Firebase.storage.reference.child(imageUrl)
                val ONE_MEGABYTE: Long = 1024 * 1024
                rf.getBytes(ONE_MEGABYTE).addOnSuccessListener {
                    val bitmap = BitmapFactory.decodeByteArray(it, 0, it.size)
                    Log.i("sharebind", "${bitmap.width} * ${bitmap.height}")
                    val reviewImagelayoutparams = reviewImg.layoutParams
                    reviewImagelayoutparams.width = bitmap.width * 30 / 100
                    reviewImagelayoutparams.height = bitmap.height * 30 / 100
                    reviewImg.layoutParams = reviewImagelayoutparams
                    reviewImg.setImageBitmap(bitmap)
                }
            }
        }

    inner class MyShareMessageViewHolder(itemView: SharemessageMineBinding) :
        RecyclerView.ViewHolder(itemView.root){
            var restaurantNameTxt = itemView.restaurantName
            var restaurantAddressTxt = itemView.restaurantAddress
            var reviewImg = itemView.reviewImg
            var timeTxt = itemView.txtDate
            fun bind(position: Int){
                var contentList = items[position].message!!.split("|")
                var restaurantName = contentList[0]
                var restaurantAddress = contentList[1]
                var imageUrl = contentList[2]


                restaurantNameTxt.text = restaurantName
                restaurantAddressTxt.text = restaurantAddress
                timeTxt.text = getTime(items[position].time)
                val rf = Firebase.storage.reference.child(imageUrl)
                val ONE_MEGABYTE: Long = 1024 * 1024
                rf.getBytes(ONE_MEGABYTE).addOnSuccessListener {
                    val bitmap = BitmapFactory.decodeByteArray(it, 0, it.size)
                    Log.i("sharebind", "${bitmap.width} * ${bitmap.height}")
                    val reviewImagelayoutparams = reviewImg.layoutParams
                    reviewImagelayoutparams.width = bitmap.width * 30 / 100
                    reviewImagelayoutparams.height = bitmap.height * 30 / 100
                    reviewImg.layoutParams = reviewImagelayoutparams
                    reviewImg.setImageBitmap(bitmap)
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

            if(items[position].profanity){
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
    }
}