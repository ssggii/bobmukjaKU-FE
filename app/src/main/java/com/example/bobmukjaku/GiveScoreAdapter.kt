package com.example.bobmukjaku

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.bobmukjaku.Model.UpdateScoreInfo
import com.example.bobmukjaku.databinding.GiveScoreItemBinding

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
        private val thumbUpView = itemView.thumbup
        private val thumbDownView = itemView.thumbdown

        fun bind(position: Int){
            nameView.text = participantsInfo[position].participant.memberNickName
            levelView.text = "LV ".plus(participantsInfo[position].participant.rate.toString())
            profileView.background = context.getDrawable(
                context.resources.getIdentifier("bg1","drawable", context.packageName))

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