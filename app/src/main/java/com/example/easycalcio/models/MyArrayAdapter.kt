package com.example.easycalcio.models

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.example.easycalcio.R

class MyArrayAdapter(context: Context, val resource: Int, val matches: List<Match>) : ArrayAdapter<Match>(context, resource, matches){
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val match : Match = matches.get(position)
        var view : View? = convertView

        if(view == null){
            view = LayoutInflater.from(context).inflate(R.layout.match_layout, parent, false)
        }

        //TODO: edit the match_layout text views

        return view
    }
}