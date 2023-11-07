package com.bobmukja.bobmukjaku

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bobmukja.bobmukjaku.Dto.FriendInfoDto
import com.bobmukja.bobmukjaku.Dto.FriendUpdateDto
import com.bobmukja.bobmukjaku.Model.*
import com.bobmukja.bobmukjaku.databinding.FriendListBinding
import com.google.firebase.messaging.FirebaseMessaging
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FriendListAdapter(var items: List<FriendInfoDto>, var onFriendRemovedListener: OnFriendRemovedListener): RecyclerView.Adapter<FriendListAdapter.ViewHolder>() {

    private val accessToken = SharedPreferences.getString("accessToken", "")
    private val authorizationHeader = "Bearer $accessToken"

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

    @SuppressLint("ClickableViewAccessibility")
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

        // 친구 해제 스와이프 이벤트
        holder.binding.root.setOnTouchListener { _, motionEvent ->
            if (motionEvent.action == MotionEvent.ACTION_DOWN) {
                // 사용자가 스와이프를 시작하면 친구 해제 동작을 수행
                val dialogView = LayoutInflater.from(holder.binding.root.context).inflate(R.layout.friend_delete_dialog, null)
                val yesButton = dialogView.findViewById<TextView>(R.id.time_btn_yes)
                val noButton = dialogView.findViewById<TextView>(R.id.time_btn_no)

                val builder = AlertDialog.Builder(holder.binding.root.context)
                    .setView(dialogView)
                    .setCancelable(false)

                val alertDialog = builder.create()
                alertDialog.show()

                // 확인 버튼 클릭 이벤트 처리
                yesButton.setOnClickListener {
                    val friendCheck = FriendUpdateDto(friendUid = friendInfo.friendUid)
                    RetrofitClient.friendService.removeBlock(authorizationHeader, friendCheck).enqueue(object :
                        Callback<Void> {
                        override fun onResponse(call: Call<Void>, response: Response<Void>) {
                            if (response.isSuccessful) {
                                // 성공적으로 밥친구 해제 완료
                                Toast.makeText(holder.binding.root.context, "밥친구 해제가 완료되었습니다.", Toast.LENGTH_SHORT).show()

                                // 밥친구 해제한 아이템의 위치를 리스너를 통해 알림
                                onFriendRemovedListener.onFriendRemoved(position)
                            } else {
                                Toast.makeText(
                                    holder.binding.root.context,
                                    "밥친구 해제에 실패했습니다. 다시 시도해주세요.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }

                        override fun onFailure(call: Call<Void>, t: Throwable) {
                            // 네트워크 오류 또는 기타 에러가 발생했을 때의 처리
                            t.message?.let { Log.i("[밥친구 해제 실패: ]", it) }
                        }
                    })
                    alertDialog.dismiss()
                }

                // 취소 버튼 클릭 이벤트 처리
                noButton.setOnClickListener {
                    alertDialog.dismiss()
                }
                true // 스와이프 동작을 소비
            } else {
                false // 다른 동작은 그대로 처리
            }
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }
}