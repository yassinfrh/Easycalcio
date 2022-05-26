package com.example.easycalcio.models

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.easycalcio.R
import com.example.easycalcio.activities.FriendProfileActivity
import com.example.easycalcio.fragments.FriendRequestsFragment
import kotlinx.coroutines.*

class FriendRequestsArrayAdapter(context: Context, val resource: Int, val users: List<User>) :
    ArrayAdapter<User>(context, resource, users) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val user: User = users[position]
        var view: View? = convertView
        if (view == null) {
            view =
                LayoutInflater.from(context).inflate(R.layout.friend_request_layout, parent, false)
        }

        val friendUsername: TextView = view!!.findViewById(R.id.friendUsername)
        friendUsername.text = user.username

        val acceptButton: Button = view.findViewById(R.id.acceptButton)
        acceptButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                CoroutineScope(Dispatchers.Main + Job()).launch {
                    withContext(Dispatchers.IO) {
                        acceptFriendRequest(view.context, user.username)
                        withContext(Dispatchers.Main) {
                            val activity : AppCompatActivity = context as AppCompatActivity
                            activity.supportFragmentManager.beginTransaction().replace(R.id.fragment_container, FriendRequestsFragment()).commit()
                        }
                    }
                }
            }

        })

        val declineButton: Button = view.findViewById(R.id.declineButton)
        declineButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                CoroutineScope(Dispatchers.Main + Job()).launch {
                    withContext(Dispatchers.IO) {
                        declineFriendRequest(view.context, user.username)
                        withContext(Dispatchers.Main) {
                            val activity : AppCompatActivity = context as AppCompatActivity
                            activity.supportFragmentManager.beginTransaction().replace(R.id.fragment_container, FriendRequestsFragment()).commit()
                        }
                    }
                }
            }

        })

        val profilePicture : ImageView = view.findViewById(R.id.profilePicture)
        profilePicture.setOnClickListener(object : View.OnClickListener{
            override fun onClick(v: View?) {
                val intent = Intent(view.context, FriendProfileActivity::class.java)
                intent.putExtra("username", user.username)
                view.context.startActivity(intent)
            }

        })
        //TODO: replace profile picture

        return view
    }
}