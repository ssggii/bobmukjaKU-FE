package com.example.bobmukjaku

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bobmukjaku.Dto.FriendInfoDto
import com.example.bobmukjaku.Dto.FriendUpdateDto
import com.example.bobmukjaku.Model.*
import com.example.bobmukjaku.databinding.FragmentFriendListBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class FriendListFragment : Fragment(), FriendListAdapter.OnFriendRemovedListener {

    lateinit var mContext: Context
    lateinit var binding: FragmentFriendListBinding
    lateinit var adapter: FriendListAdapter
    var friendList = mutableListOf<FriendInfoDto>()

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
        binding = FragmentFriendListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getFriendList()
    }

    // 친구 추가 테스트 코드
    private fun addFriend() {
        val friendInfo = FriendUpdateDto(friendUid = 2)
        val call = friendService.registerFriend(authorizationHeader, friendInfo)
        call.enqueue(object : Callback<Unit> {
            override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                if (response.isSuccessful) {
                    Log.i("RegisterFriend", "친구 등록 완료")
                } else {
                    val errorCode = response.code()
                    Log.i("RegisterFriend", "친구 등록 실패 $errorCode")
                }
            }

            override fun onFailure(call: Call<Unit>, t: Throwable) {
                // 네트워크 오류 또는 기타 에러가 발생했을 때의 처리
                t.message?.let { it1 -> Log.i("[친구 등록 기타 에러: ]", it1) }
            }
        })
    }
    private fun getFriendList() {
        val call = friendService.getFriendList(authorizationHeader)

        call.enqueue(object : Callback<List<FriendInfoDto>> {
            override fun onResponse(call: Call<List<FriendInfoDto>>, response: Response<List<FriendInfoDto>>) {
                if (response.isSuccessful) {
                    val friendListResponse = response.body() // 서버에서 받은 친구 목록
                    if (friendListResponse != null) {
                        friendList.clear()
                        friendList.addAll(friendListResponse) // friendList에 업데이트된 친구 목록 저장
                        adapter.updateItems(friendList) // 어댑터에 업데이트된 목록 전달

                        binding.totalFriend.text = friendList.size.toString()
                    }
                    val successCode = response.code()
                    Log.i("내 친구 목록 로드", "성공 $successCode")
                } else {
                    val errorCode = response.code()
                    Log.i("내 친구 목록 로드", "에러 $errorCode")
                }
            }

            override fun onFailure(call: Call<List<FriendInfoDto>>, t: Throwable) {
                // 네트워크 오류 또는 기타 에러가 발생했을 때의 처리
                t.message?.let { it1 -> Log.i("[내 친구 목록 로드 에러: ]", it1) }
            }
        })

        binding.friendRecyclerView.layoutManager = LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false)
        adapter = FriendListAdapter(friendList, this@FriendListFragment)
        adapter.onItemClickListener = object : FriendListAdapter.OnItemClickListener {
            override fun onItemClick(pos: Int, friendInfo: FriendInfoDto) {

            }
        }
        binding.friendRecyclerView.adapter = adapter
    }


    override fun onFriendRemoved(position: Int) {
        friendList.removeAt(position)
        binding.totalFriend.text = friendList.size.toString()
        adapter.notifyDataSetChanged()
    }
}