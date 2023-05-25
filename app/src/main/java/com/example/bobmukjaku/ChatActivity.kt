package com.example.bobmukjaku

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bobmukjaku.Model.ChatModel
import com.example.bobmukjaku.databinding.ActivityChatBinding

class ChatActivity : AppCompatActivity() {
    lateinit var binding: ActivityChatBinding
    //lateinit var auth:FirebaseAuth
    lateinit var adapter:ChatAdapter

    lateinit var myUid:String
    lateinit var yourUid:String
    lateinit var name:String

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
        //auth = FirebaseAuth.getInstance()
        //myUid = auth.uid!!
        yourUid = intent.getStringExtra("uid")!!
        name = intent.getStringExtra("name")!!
        chatItem = arrayListOf<ChatModel>()

//        Firebase.database.getReference("message/$myUid/$yourUid")
//            .get()
//            .addOnSuccessListener{
//                chatItem.clear()
//                for (chat in it.children) {
//                    val myUid = chat.child("myUid").value.toString()
//                    val yourUid = chat.child("yourUid").value.toString()
//                    val message = chat.child("message").value.toString()
//                    val time = chat.child("time").value.toString().toLong()
//                    val who = chat.child("who").toString()
//                    chatItem.add(ChatModel(myUid, yourUid, message, time, who))
//                }
//                adapter.notifyDataSetChanged()
//                Toast.makeText(this@ChatActivity, "test", Toast.LENGTH_SHORT).show()
//            }

//        val rf = Firebase.database.getReference("message/$myUid/$yourUid")
//        val childEventListener = object:ChildEventListener{
//            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
//                //Log.i("chat", snapshot.toString())
//                val myUid = snapshot.child("myUid").value.toString()
//                val yourUid = snapshot.child("yourUid").value.toString()
//                val message = snapshot.child("message").value.toString()
//                val time = snapshot.child("time").value.toString().toLong()
//                val who = snapshot.child("who").toString()
//                chatItem.add(ChatModel(myUid, yourUid, message, time, who))
//                adapter.notifyDataSetChanged()
//                binding.chatRecyclerView.scrollToPosition(chatItem.size - 1)
//
//            }
//
//            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
//                TODO("Not yet implemented")
//            }
//
//            override fun onChildRemoved(snapshot: DataSnapshot) {
//                TODO("Not yet implemented")
//            }
//
//            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
//                TODO("Not yet implemented")
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                TODO("Not yet implemented")
//            }
//
//        }
//        rf.addChildEventListener(childEventListener)

        /*Firebase.database.getReference("message")
            .get()
            .addOnSuccessListener{
                   for (chat in it.children) {
                       val myUid = chat.child("myUid").value.toString()
                       val yourUid = chat.child("yourUid").value.toString()
                       val message = chat.child("message").value.toString()
                       val time = chat.child("time").value.toString().toLong()
                       val who = chat.child("who").toString()
                       chatItem.add(ChatModel(myUid, yourUid, message, time, who))
                   }
                   adapter.notifyDataSetChanged()
                   Toast.makeText(this@ChatRoomActivity, "test", Toast.LENGTH_SHORT).show()
               }*/
    }

    private fun initLayout() {

        binding.apply {
            sendMsg.setOnClickListener {
                val message = this.message.text.toString()
                this.message.setText("")

                //val rf = Firebase.database.getReference("message")
                val chatSend = ChatModel(myUid, yourUid, message, System.currentTimeMillis(), "me")
                //rf.child(myUid).child(yourUid).push().setValue(chatSend)

                val chatGet = ChatModel(myUid, yourUid, message, System.currentTimeMillis(), "you")
                //rf.child(yourUid).child(myUid).push().setValue(chatGet)

                /* val chat = ChatModel(myUid, yourUid, message, System.currentTimeMillis())
                 Firebase.database.getReference("message")
                     .push()
                     .setValue(chat)
                     .addOnSuccessListener {
                         Toast.makeText(this@ChatRoomActivity, "성공", Toast.LENGTH_SHORT).show()
                     }
                     .addOnFailureListener {
                         Toast.makeText(this@ChatRoomActivity, "실패", Toast.LENGTH_SHORT).show()
                     }*/
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