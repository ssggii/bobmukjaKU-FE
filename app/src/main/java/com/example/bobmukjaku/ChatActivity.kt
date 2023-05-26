package com.example.bobmukjaku

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bobmukjaku.Model.ChatModel
import com.example.bobmukjaku.databinding.ActivityChatBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class ChatActivity : AppCompatActivity() {
    lateinit var binding: ActivityChatBinding
    lateinit var auth:FirebaseAuth
    lateinit var adapter:ChatAdapter

    lateinit var myUid:String
    lateinit var myName:String
    lateinit var yourUid:String
    lateinit var yourName:String


    lateinit var chatItem:ArrayList<ChatModel>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initData()
        initLayout()
        initRecyclerView()
    }

    private fun initData() {
        auth = FirebaseAuth.getInstance()
        myUid = auth.uid!!
        yourUid = intent.getStringExtra("uid")!!
        yourName = intent.getStringExtra("name")!!
        chatItem = arrayListOf<ChatModel>()



        Firebase.database.getReference("users/$myUid/username")
            .get()
            .addOnSuccessListener {
                myName = it.value.toString()
            }


        Firebase.database.getReference("message/$myUid/$yourUid")
            .get()
            .addOnSuccessListener{
                chatItem.clear()
                for (chat in it.children) {
                    val myUid = chat.child("myUid").value.toString()
                    val yourUid = chat.child("yourUid").value.toString()
                    val message = chat.child("message").value.toString()
                    val time = chat.child("time").value.toString().toLong()
                    val who = chat.child("who").value.toString()
                    val senderName = chat.child("senderName").value.toString()
                    chatItem.add(ChatModel(myUid, yourUid, message, time, who, senderName))
                }
                adapter.notifyDataSetChanged()

            }

        val rf = Firebase.database.getReference("message/$myUid/$yourUid")
        val childEventListener = object: ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                //Log.i("chat", snapshot.toString())
                val myUid = snapshot.child("myUid").value.toString()
                val yourUid = snapshot.child("yourUid").value.toString()
                val message = snapshot.child("message").value.toString()
                val time = snapshot.child("time").value.toString().toLong()
                val who = snapshot.child("who").toString()
                val senderName = snapshot.child("senderName").value.toString()
                chatItem.add(ChatModel(myUid, yourUid, message, time, who, senderName))
                adapter.notifyDataSetChanged()
                binding.chatRecyclerView.scrollToPosition(chatItem.size - 1)

            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                TODO("Not yet implemented")
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                TODO("Not yet implemented")
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                TODO("Not yet implemented")
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }


        }
        rf.addChildEventListener(childEventListener)

    }

    private fun initLayout() {

        binding.apply {
            sendMsg.setOnClickListener {
                val message = this.message.text.toString()
                this.message.setText("")

                val rf = Firebase.database.getReference("message")
                val chatSend = ChatModel(myUid, yourUid, message, System.currentTimeMillis(), "me", myName)
                rf.child(myUid).child(yourUid).push().setValue(chatSend)

                val chatGet = ChatModel(myUid, yourUid, message, System.currentTimeMillis(), "you", myName)
                rf.child(yourUid).child(myUid).push().setValue(chatGet)


            }
        }
    }

    private fun initRecyclerView() {

        val layoutManager = LinearLayoutManager(this@ChatActivity, LinearLayoutManager.VERTICAL, false)
        layoutManager.stackFromEnd = true
        binding.chatRecyclerView.layoutManager = layoutManager
        adapter = ChatAdapter(chatItem)
        binding.chatRecyclerView.adapter = adapter
        binding.chatRecyclerView.scrollToPosition(chatItem.size - 1)
    }
}