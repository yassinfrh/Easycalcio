package com.example.easycalcio.models

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.easycalcio.R
import com.example.easycalcio.activities.FriendProfileActivity
import com.example.easycalcio.activities.MainActivity
import kotlinx.coroutines.*

class MatchPlayersArrayAdapter(context: Context, val resource: Int, val players: List<String>) : ArrayAdapter<String>(context, resource, players) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val player: String = players[position]
        var view: View? = convertView

        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.match_player_layout, parent, false)
        }

        val playerUsername: TextView = view!!.findViewById(R.id.playerUsername)
        playerUsername.text = player

        view.setOnClickListener(object : View.OnClickListener{
            override fun onClick(v: View?) {
                CoroutineScope(Dispatchers.Main + Job()).launch {
                    withContext(Dispatchers.IO) {
                        val user = getUser(view.context)
                        withContext(Dispatchers.Main) {
                            if(user.username != player){
                                val intent = Intent(view.context, FriendProfileActivity::class.java)
                                intent.putExtra("username", player)
                                view.context.startActivity(intent)
                            }
                        }
                    }
                }
            }

        })

        return view
    }
}