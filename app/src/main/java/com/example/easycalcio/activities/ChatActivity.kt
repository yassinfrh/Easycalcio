package com.example.easycalcio.activities

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.easycalcio.R
import com.example.easycalcio.models.*
import com.firebase.ui.database.FirebaseListAdapter
import com.firebase.ui.database.FirebaseListOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.Query
import kotlinx.coroutines.*
import java.util.*


class ChatActivity : AppCompatActivity() {

    private var adapter: FirebaseListAdapter<Message>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        title = "Chat"

        val friendUsername: String = intent.getStringExtra("username")!!
        val myUsername: String = intent.getStringExtra("muUsername")!!
        val chatId: Long = intent.getLongExtra("chatId", 0.toLong())

        val messagesListView: ListView = findViewById(R.id.chatMessageList)
        val query: Query =
            FirebaseDbWrapper(this@ChatActivity).dbRef.child("chats").child(chatId.toString())
                .child("messages")
        val options: FirebaseListOptions<Message> =
            FirebaseListOptions.Builder<Message>().setLayout(R.layout.message_layout)
                .setQuery(query, Message::class.java).build()
        adapter = object : FirebaseListAdapter<Message>(options) {
            override fun populateView(v: View, model: Message, position: Int) {

                val messageText: TextView = v.findViewById(R.id.message_text)
                val messageUser: TextView = v.findViewById(R.id.message_user)
                val messageTime: TextView = v.findViewById(R.id.message_time)

                messageText.text = model.text
                messageUser.text = model.sender
                messageTime.text = model.formattedDate
            }

        }

        messagesListView.adapter = adapter

        GlobalScope.launch {
            readMessages(this@ChatActivity, chatId)
        }

        val sendButton: FloatingActionButton = findViewById(R.id.chatSendButton)
        sendButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View?) {
                val messageEditText: EditText = findViewById(R.id.chatMessageEditText)
                if (messageEditText.text.isNotBlank()) {
                    CoroutineScope(Dispatchers.Main + Job()).launch {
                        withContext(Dispatchers.IO) {
                            sendMessage(
                                Message(
                                    myUsername,
                                    messageEditText.text.toString(),
                                    Date(),
                                    false
                                ), friendUsername, chatId, this@ChatActivity
                            )
                            withContext(Dispatchers.Main) {
                                adapter!!.notifyDataSetChanged()
                                messageEditText.setText("")
                            }
                        }
                    }
                }
            }
        })
    }

    override fun onStart() {
        super.onStart()
        adapter!!.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter!!.stopListening()
    }
}