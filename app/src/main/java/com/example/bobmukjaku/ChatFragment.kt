package com.example.bobmukjaku

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bobmukjaku.Model.UserItem
import com.example.bobmukjaku.databinding.FragmentChatBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class ChatFragment : Fragment() {

    lateinit var mContext: Context
    lateinit var binding: FragmentChatBinding
    lateinit var adapter: ChatRoomListAdapter
    var chatlist = arrayListOf<UserItem>()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getChatRoomList()
        makeChatRoom()
    }

    private fun makeChatRoom() {
        // 모집방 개설 버튼 클릭 이벤트 처리
        binding.openRoomBtn.setOnClickListener {
            val intent = Intent(requireContext(), MakeRoomActivity::class.java)
            startActivity(intent)
        }
    }

    private fun getChatRoomList() {
        val db = Firebase.database.getReference("users")
        db.get().addOnSuccessListener { dataSnapshot: DataSnapshot ->
            for (user in dataSnapshot.children) {
                val name = user.child("username").value.toString()
                val uid = user.child("uid").value.toString()
                Log.i("user", name.plus(uid))
                chatlist.add(UserItem(name, "message", uid))
            }


                binding.joinRecyclerView.layoutManager = LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false)
                adapter = ChatRoomListAdapter(chatlist)
                adapter.onItemClickListener = object:ChatRoomListAdapter.OnItemClickListener{
                    override fun onItemClick(pos: Int) {
                        val intent = Intent(requireActivity(), ChatActivity::class.java)
                        intent.putExtra("name", chatlist[pos].name)
                        intent.putExtra("uid", chatlist[pos].uid)
                        startActivity(intent)
                    }

                }
                binding.joinRecyclerView.adapter = adapter
            }
    }
}