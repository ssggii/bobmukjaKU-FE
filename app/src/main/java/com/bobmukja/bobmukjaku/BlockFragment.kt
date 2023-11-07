package com.bobmukja.bobmukjaku

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.bobmukja.bobmukjaku.Dto.BlockInfoDto
import com.bobmukja.bobmukjaku.Dto.FriendInfoDto
import com.bobmukja.bobmukjaku.Dto.FriendUpdateDto
import com.bobmukja.bobmukjaku.Model.*
import com.bobmukja.bobmukjaku.databinding.FragmentBlockBinding
import com.bobmukja.bobmukjaku.databinding.FragmentFriendListBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class BlockFragment : Fragment(), BlockAdapter.OnBlockRemovedListener {

    lateinit var mContext: Context
    lateinit var binding: FragmentBlockBinding
    lateinit var adapter: BlockAdapter
    var blockList = mutableListOf<BlockInfoDto>()

    private val friendService = RetrofitClient.friendService
    private val accessToken = SharedPreferences.getString("accessToken", "")
    private val authorizationHeader = "Bearer $accessToken"

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentBlockBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getBlockList()
    }

    private fun getBlockList() {
        val call = friendService.getBlockList(authorizationHeader)

        call.enqueue(object : Callback<List<BlockInfoDto>> {
            override fun onResponse(call: Call<List<BlockInfoDto>>, response: Response<List<BlockInfoDto>>) {
                if (response.isSuccessful) {
                    val blockListResponse = response.body() // 서버에서 받은 친구 목록
                    if (blockListResponse != null) {
                        blockList.clear()
                        blockList.addAll(blockListResponse) // friendList에 업데이트된 친구 목록 저장
                        adapter.updateItems(blockList) // 어댑터에 업데이트된 목록 전달

                        binding.totalBlock.text = blockList.size.toString()
                    } else {
                        blockList.clear()
                        adapter.updateItems(blockList)
                    }
                    val successCode = response.code()
                    Log.i("내 차단 목록 로드", "성공 $successCode")
                } else {
                    val errorCode = response.code()
                    Log.i("내 차단 목록 로드", "에러 $errorCode")
                }
            }

            override fun onFailure(call: Call<List<BlockInfoDto>>, t: Throwable) {
                // 네트워크 오류 또는 기타 에러가 발생했을 때의 처리
                t.message?.let { it1 -> Log.i("[내 차단 목록 로드 에러: ]", it1) }
            }
        })

        binding.blockRecyclerView.layoutManager = LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false)
        adapter =
            BlockAdapter(blockList, this@BlockFragment)
        adapter.onItemClickListener = object : BlockAdapter.OnItemClickListener {
            override fun onItemClick(pos: Int, friendInfo: FriendInfoDto) {

            }
        }
        binding.blockRecyclerView.adapter = adapter
    }


    override fun onBlockRemoved(position: Int) {
        blockList.removeAt(position)
        binding.totalBlock.text = blockList.size.toString()
        adapter.notifyDataSetChanged()
    }
}