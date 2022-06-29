package com.example.easycalcio.activities

import android.content.Intent
import android.media.Image
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.example.easycalcio.R
import com.example.easycalcio.models.*
import kotlinx.coroutines.*

class FriendProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friend_profile)
        title = "User profile"

        val username = intent.getStringExtra("username")!!

        val usernameView: TextView = findViewById(R.id.friendProfileUsername)
        val nameView: TextView = findViewById(R.id.friendProfileName)
        val surnameView: TextView = findViewById(R.id.friendProfileSurname)
        val ageView: TextView = findViewById(R.id.friendProfileAge)
        val cityView: TextView = findViewById(R.id.friendProfileCity)
        val roleView: TextView = findViewById(R.id.friendProfileRole)


        var chatId: Long? = null
        var myUsername: String? = null

        val addFriendButton: Button = findViewById(R.id.friendProfileAddRemoveFriendButton)
        val chatButton : Button = findViewById(R.id.friendProfileChatButton)
        val profileImage : ImageView = findViewById(R.id.profileImage)

        CoroutineScope(Dispatchers.Main + Job()).launch {
            withContext(Dispatchers.IO) {
                val user = getUserWithUsername(this@FriendProfileActivity, username)
                val currUser = getUser(this@FriendProfileActivity)
                myUsername = currUser.username
                chatId = getChatId(myUsername!!, user.username, this@FriendProfileActivity)
                val image = FirebaseStorageWrapper().download(user.username.lowercase())
                withContext(Dispatchers.Main) {
                    usernameView.text = user.username
                    nameView.text = user.name
                    surnameView.text = user.surname
                    ageView.text = user.getAge().toString()
                    cityView.text = user.city
                    roleView.text = user.role
                    if (currUser.friends != null && currUser.friends!!.contains(username)) {
                        addFriendButton.text = getString(R.string.friend_profile_remove_friend)
                    }
                    chatButton.isEnabled = true
                    if(image != null){
                        profileImage.setImageURI(image)
                    }
                }
            }
        }

        CoroutineScope(Dispatchers.Main + Job()).launch {
            withContext(Dispatchers.IO) {
                val hasSent = hasSentRequest(this@FriendProfileActivity, username)
                withContext(Dispatchers.Main) {
                    if (hasSent) {
                        addFriendButton.text = getString(R.string.friend_profile_request_sent)
                        addFriendButton.isEnabled = false
                    }
                }
            }
        }

        CoroutineScope(Dispatchers.Main + Job()).launch {
            withContext(Dispatchers.IO) {
                val requests = getReceivedRequests(this@FriendProfileActivity)
                withContext(Dispatchers.Main) {
                    if (requests != null) {
                        for (user in requests) {
                            if (user.username == username) {
                                addFriendButton.text =
                                    getString(R.string.friend_profile_request_received)
                                addFriendButton.isEnabled = false
                                break
                            }
                        }
                    }
                }
            }
        }

        addFriendButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View?) {
                if(addFriendButton.text == getString(R.string.friend_profile_add_friend)){
                    CoroutineScope(Dispatchers.Main + Job()).launch {
                        withContext(Dispatchers.IO) {
                            sendFriendRequest(this@FriendProfileActivity, username)
                            withContext(Dispatchers.Main) {
                                addFriendButton.text = getString(R.string.friend_profile_request_sent)
                                addFriendButton.isEnabled = false
                            }
                        }
                    }
                }
                if(addFriendButton.text == getString(R.string.friend_profile_remove_friend)){
                    CoroutineScope(Dispatchers.Main + Job()).launch {
                        withContext(Dispatchers.IO) {
                            removeFriend(this@FriendProfileActivity, username)
                            withContext(Dispatchers.Main) {
                                addFriendButton.text = getString(R.string.friend_profile_add_friend)
                                addFriendButton.isEnabled = true
                            }
                        }
                    }
                }
            }
        })

        chatButton.setOnClickListener(object : View.OnClickListener{
            override fun onClick(v: View?) {
                val intent = Intent(v!!.context, ChatActivity::class.java)
                intent.putExtra("username", username)
                intent.putExtra("muUsername", myUsername)
                intent.putExtra("chatId", chatId)
                v.context.startActivity(intent)
            }

        })
    }
}