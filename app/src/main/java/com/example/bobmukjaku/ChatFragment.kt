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

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ChatFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ChatFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    lateinit var mContext: Context
    lateinit var binding: FragmentChatBinding
    lateinit var adapter: ChatRoomListAdapter
    var chatlist = arrayListOf<UserItem>()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

        getChatRoomList()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
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

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ChatFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ChatFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}