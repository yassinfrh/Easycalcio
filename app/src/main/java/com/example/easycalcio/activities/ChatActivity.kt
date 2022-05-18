package com.example.easycalcio.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.view.View
import android.widget.EditText
import com.example.easycalcio.R
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ChatActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        title = "Chat"

        //TODO: display the chat

        val sendButton : FloatingActionButton = findViewById(R.id.chatSendButton)
        sendButton.setOnClickListener(object : View.OnClickListener{
            override fun onClick(view: View?) {
                val messageEditText : EditText = findViewById(R.id.chatMessageEditText)
                //TODO: add to database

                messageEditText.setText("")
            }

        })
    }
}