package com.example.easycalcio.models

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.easycalcio.R
import com.example.easycalcio.activities.EditMatchActivity
import kotlinx.coroutines.*

class UsersArrayAdapter(context: Context, val resource: Int, val users: List<User>) :
    ArrayAdapter<User>(context, resource, users) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val user: User = users[position]
        var view: View? = convertView

        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.friend_layout, parent, false)
        }

        //TODO: replace profile picture

        val friendName: TextView = view!!.findViewById(R.id.friendName)
        friendName.text = "${user.name} ${user.surname}"

        val friendUsername: TextView = view.findViewById(R.id.friendUsername)
        friendUsername.text = user.username

        val friendRole: TextView = view.findViewById(R.id.friendRole)
        friendRole.text = user.role

        val activity = view.context
        if (activity is EditMatchActivity) {
            //set players background to green and add them to selected friends
            CoroutineScope(Dispatchers.Main + Job()).launch {
                withContext(Dispatchers.IO) {
                    val friends = getFriendsList(view.context)
                    val match = getMatch(view.context, activity.matchId!!)
                    withContext(Dispatchers.Main) {
                        if(friends != null){
                            val username = friendUsername.text.toString()
                            if (match.players!!.contains(username)){
                                if(activity.selectedFriends == null){
                                    activity.selectedFriends = mutableListOf(username)
                                    view.setBackgroundColor(Color.parseColor("#79f29d"))
                                }
                                else{
                                    if(!activity.selectedFriends!!.contains(username)){
                                        activity.selectedFriends!!.add(username)
                                        view.setBackgroundColor(Color.parseColor("#79f29d"))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return view
    }
}
