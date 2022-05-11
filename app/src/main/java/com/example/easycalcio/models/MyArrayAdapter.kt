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
import java.util.*

class MyArrayAdapter(context: Context, val resource: Int, val matches: List<Match>) : ArrayAdapter<Match>(context, resource, matches){
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val match : Match = matches[position]
        var view : View? = convertView

        if(view == null){
            view = LayoutInflater.from(context).inflate(R.layout.match_layout, parent, false)
        }

        val matchTitle: TextView = view!!.findViewById(R.id.matchTitle)
        matchTitle.text = match.title

        val matchDate: TextView = view.findViewById(R.id.matchDate)
        matchDate.text = match.formattedDate

        val matchLocation: TextView = view.findViewById(R.id.matchLocation)
        matchLocation.text = getAddress(match)

        return view
    }

    private fun getAddress(match: Match) : String{
        val geocoder = Geocoder(this.context, Locale.getDefault())
        val addresses : List<Address> = geocoder.getFromLocation(match.location.latitude, match.location.longitude, 1)
        return "${addresses[0].getAddressLine(0)}, ${addresses[0].locality}"
    }
}