package com.example.bobmukjaku

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.bobmukjaku.Dto.BlockInfoDto
import com.example.bobmukjaku.Dto.FriendInfoDto
import com.example.bobmukjaku.Dto.FriendUpdateDto
import com.example.bobmukjaku.Model.*
import com.example.bobmukjaku.databinding.BlockListBinding
import com.example.bobmukjaku.databinding.FriendListBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class BlockAdapter(var items: List<BlockInfoDto>, var onBlockRemovedListener: OnBlockRemovedListener): RecyclerView.Adapter<BlockAdapter.ViewHolder>() {

    private val accessToken = SharedPreferences.getString("accessToken", "")
    private val authorizationHeader = "Bearer $accessToken"

    interface OnItemClickListener{
        fun onItemClick(pos: Int, friendInfo: FriendInfoDto)
    }

    var onItemClickListener:OnItemClickListener? = null

    interface OnBlockRemovedListener {
        fun onBlockRemoved(position: Int)
    }

    inner class ViewHolder(var binding: BlockListBinding): RecyclerView.ViewHolder(binding.root){
    }

    fun updateItems(newItems: List<BlockInfoDto>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = BlockListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val blockInfo = items[position]
        val bgResourceId = when (blockInfo.blockProfileColor) {
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
        holder.binding.tvItemChattingName.text = blockInfo.blockNickname

        var level = blockInfo.blockRate.toString().toInt()
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

        // 차단 해제 버튼 이벤트
        holder.binding.blockBtn.setOnClickListener {
            val blockInfo = FriendUpdateDto(friendUid = blockInfo.blockUid)
            RetrofitClient.friendService.removeBlock(authorizationHeader, blockInfo).enqueue(object :
                Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        // 성공적으로 차단 해제 완료
                        Toast.makeText(holder.binding.root.context, "차단 해제가 완료되었습니다.", Toast.LENGTH_SHORT).show()

                        // 스크랩 해제한 아이템의 위치를 리스너를 통해 알림
                        onBlockRemovedListener.onBlockRemoved(position)
                    } else {
                        val errorCode = response.code()
                        Toast.makeText(
                            holder.binding.root.context,
                            "차단 해제에 실패했습니다. 에러 코드: $errorCode",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    // 네트워크 오류 또는 기타 에러가 발생했을 때의 처리
                    t.message?.let { Log.i("[스크랩 해제 실패: ]", it) }
                }
            })
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }
}