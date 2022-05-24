package com.example.easycalcio.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
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

        val addFriendButton: Button = findViewById(R.id.friendProfileAddRemoveFriendButton)

        CoroutineScope(Dispatchers.Main + Job()).launch {
            withContext(Dispatchers.IO) {
                val user = getUserWithUsername(this@FriendProfileActivity, username)
                val currUser = getUser(this@FriendProfileActivity)
                withContext(Dispatchers.Main) {
                    usernameView.text = user.username
                    nameView.text = user.name
                    surnameView.text = user.surname
                    ageView.text = user.getAge().toString()
                    cityView.text = user.city
                    roleView.text = user.role
                    if (currUser.friends.contains(username)) {
                        addFriendButton.text = getString(R.string.friend_profile_remove_friend)
                    }
                }
            }
        }

        CoroutineScope(Dispatchers.Main + Job()).launch {
            withContext(Dispatchers.IO) {
                val hasSent = hasSentRequest(this@FriendProfileActivity, username)
                withContext(Dispatchers.Main) {
                    if(hasSent){
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
                    if(requests != null){
                        for(user in requests){
                            if(user.username == username){
                                addFriendButton.text = getString(R.string.friend_profile_request_received)
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
        })

        //TODO: add chat button listener
    }
}