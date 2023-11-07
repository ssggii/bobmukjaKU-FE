package com.bobmukja.bobmukjaku

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bobmukja.bobmukjaku.Model.UpdateScoreInfo
import com.bobmukja.bobmukjaku.databinding.GiveScoreItemBinding

class GiveScoreAdapter(var participantsInfo: List<UpdateScoreInfo>, val context: Context): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val thumbUpNotPressed = context.getDrawable(
        context.resources.getIdentifier("baseline_thumb_up_24_not_pressed","drawable", context.packageName))
    private val thumbUpPressed = context.getDrawable(
        context.resources.getIdentifier("baseline_thumb_up_24","drawable", context.packageName))
    private val thumbDownNotPressed = context.getDrawable(
        context.resources.getIdentifier("baseline_thumb_down_24_not_pressed","drawable", context.packageName))
    private val thumbDownPressed = context.getDrawable(
        context.resources.getIdentifier("baseline_thumb_down_24","drawable", context.packageName))

    inner class ViewHolder(itemView: GiveScoreItemBinding):RecyclerView.ViewHolder(itemView.root){
        private val nameView = itemView.participantName
        private val levelView = itemView.participantLevel
        private val profileView = itemView.profile
        private val profileBgView = itemView.profileBg
        private val thumbUpView = itemView.thumbup
        private val thumbDownView = itemView.thumbdown

        fun bind(position: Int){
            nameView.text = participantsInfo[position].participant.memberNickName

            var rate = participantsInfo[position].participant.rate?:45
            var level = ""
            if (rate <= 20) {
                level = "1"
                profileView.setBackgroundResource(R.drawable.ku_1)
            } else if (rate <= 40) {
                rate -= 20
                level = "2"
                profileView.setBackgroundResource(R.drawable.ku_2)
            } else if (rate <= 60) {
                rate -= 40
                level = "3"
                profileView.setBackgroundResource(R.drawable.ku_3)
            } else if (rate <= 80) {
                rate -= 60
                level = "4"
                profileView.setBackgroundResource(R.drawable.ku_4)
            } else {
                rate -= 80
                level = "5"
                profileView.setBackgroundResource(R.drawable.ku_5)
            }

            val bgResourceId = when (participantsInfo[position].participant.profileColor) {
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
            profileBgView.setImageResource(bgResourceId)
            levelView.text = "LV ".plus(level)


            thumbUpView.setOnClickListener {
                if(participantsInfo[position].thumbDown){
                    participantsInfo[position].thumbDown = false
                    thumbDownView.background = thumbDownNotPressed
                }
                when(participantsInfo[position].thumbUp){
                    true->{
                        participantsInfo[position].thumbUp = false
                        thumbUpView.background = thumbUpNotPressed
                    }
                    false->{
                        participantsInfo[position].thumbUp = true
                        thumbUpView.background = thumbUpPressed
                    }
                }
            }

            thumbDownView.setOnClickListener {
                if(participantsInfo[position].thumbUp){
                    participantsInfo[position].thumbUp = false
                    thumbUpView.background = thumbUpNotPressed
                }
                when(participantsInfo[position].thumbDown){
                    true->{
                        participantsInfo[position].thumbDown = false
                        thumbDownView.background = thumbDownNotPressed
                    }
                    false->{
                        participantsInfo[position].thumbDown = true
                        thumbDownView.background = thumbDownPressed
                    }
                }
            }
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.give_score_item, parent, false)
        return ViewHolder(GiveScoreItemBinding.bind(view))
    }

    override fun getItemCount(): Int {
        return participantsInfo.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ViewHolder).bind(position)
    }
}