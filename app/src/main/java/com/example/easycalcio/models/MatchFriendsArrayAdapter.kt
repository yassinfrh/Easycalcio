package com.example.easycalcio.models

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.easycalcio.R

class MatchFriendsArrayAdapter {
    class MatchesArrayAdapter(context: Context, val resource: Int, val friends: List<User>) : ArrayAdapter<User>(context, resource, friends){
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val friend : User = friends[position]
            var view : View? = convertView

            if(view == null){
                view = LayoutInflater.from(context).inflate(R.layout.match_friend_layout, parent, false)
            }

            val friendName: TextView = view!!.findViewById(R.id.friendName)
            friendName.text = "${friend.name} ${friend.surname}"

            val friendUsername: TextView = view.findViewById(R.id.friendUsername)
            friendUsername.text = friend.id

            val friendRole: TextView = view.findViewById(R.id.friendRole)
            friendRole.text = friend.role

            return view
        }
    }
}