package com.example.bobmukjaku

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.bobmukjaku.Dto.FriendInfoDto
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

    var uid: Long = 0

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

        getUid()
    }

    private fun getUid() {
        val memberService = RetrofitClient.memberService

        val call = accessToken?.let { memberService.selectOne(authorizationHeader) }
        call?.enqueue(object : Callback<Member> {
            override fun onResponse(call: Call<Member>, response: Response<Member>) {
                if (response.isSuccessful) {
                    val member = response.body()
                    val uidInfo = member?.uid
                    if (uidInfo != null) {
                        uid = uidInfo
//                        getScrapList()
                    }
                } else {
                    val errorCode = response.code()
                    Toast.makeText(
                        requireContext(),
                        "uid를 가져오는데 실패했습니다. 에러 코드: $errorCode",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<Member>, t: Throwable) {
                // 네트워크 오류 또는 기타 에러가 발생했을 때의 처리
                t.message?.let { it1 -> Log.i("[uid 로드 실패: ]", it1) }
            }
        })
    }

    override fun onFriendRemoved(position: Int) {
        friendList.removeAt(position)
        binding.totalFriend.text = friendList.size.toString()
        adapter.notifyDataSetChanged()
    }
}