package com.example.easycalcio.models

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.easycalcio.R

class MatchRequestsArrayAdapter(context: Context, val resource: Int, val matches: List<Match>) :
    ArrayAdapter<Match>(context, resource, matches) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val match: Match = matches[position]
        var view: View? = convertView
        if (view == null) {
            view =
                LayoutInflater.from(context).inflate(R.layout.match_request_layout, parent, false)
        }

        //TODO: replace profile picture

        val matchUsername: TextView = view!!.findViewById(R.id.friendUsername)
        matchUsername.text = match.username

        val matchTitle: TextView = view.findViewById(R.id.matchTitle)
        matchTitle.text = match.title

        //TODO: set on click listeners

        return view
    }
}