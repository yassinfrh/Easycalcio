package com.example.easycalcio.models

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.TextView
import com.example.easycalcio.R
import com.example.easycalcio.activities.MatchInfoActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MatchRequestsArrayAdapter(context: Context, val resource: Int, val matches: List<Match>) :
    ArrayAdapter<Match>(context, resource, matches) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val match: Match = matches[position]
        var view: View? = convertView
        if (view == null) {
            view =
                LayoutInflater.from(context).inflate(R.layout.match_request_layout, parent, false)
        }

        val matchUsername: TextView = view!!.findViewById(R.id.friendUsername)
        matchUsername.text = match.username

        val matchTitle: TextView = view.findViewById(R.id.matchTitle)
        matchTitle.text = match.title

        view.setOnClickListener(object : View.OnClickListener{
            override fun onClick(v: View?) {
                val intent = Intent(view.context, MatchInfoActivity::class.java)
                intent.putExtra("matchId", match.id)
                view.context.startActivity(intent)
            }
        })

        val acceptButton : Button = view.findViewById(R.id.acceptButton)

        acceptButton.setOnClickListener(object : View.OnClickListener{
            override fun onClick(v: View?) {
                //TODO: accept request
            }

        })

        val declineButton : Button = view.findViewById(R.id.declineButton)

        declineButton.setOnClickListener(object : View.OnClickListener{
            override fun onClick(v: View?) {
                //TODO: decline request
            }

        })

        return view
    }
}