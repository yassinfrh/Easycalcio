package com.example.easycalcio.models

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.easycalcio.R
import org.w3c.dom.Text
import java.util.*

class MatchesArrayAdapter(context: Context, val resource: Int, val matches: List<Match>) :
    ArrayAdapter<Match>(context, resource, matches) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val match: Match = matches[position]
        var view: View? = convertView

        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.match_layout, parent, false)
        }

        val matchId : TextView = view!!.findViewById(R.id.matchId)
        matchId.text = match.id.toString()

        val matchOrganizer : TextView = view.findViewById(R.id.matchOrganizer)
        matchOrganizer.text = match.username

        val matchTitle: TextView = view.findViewById(R.id.matchTitle)
        matchTitle.text = match.title

        val matchDate: TextView = view.findViewById(R.id.matchDate)
        matchDate.text = match.formattedDate

        val matchLocation: TextView = view.findViewById(R.id.matchCity)
        matchLocation.text = match.city

        return view
    }
}