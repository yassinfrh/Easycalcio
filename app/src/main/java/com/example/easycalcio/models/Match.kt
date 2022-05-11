package com.example.easycalcio.models

import android.location.Location
import java.text.SimpleDateFormat
import java.util.*

class Match (val id : Long, val userId : Long, val date: Date, val place : Location, val players : List<User>){

    private val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)

    val formattedDate : String?
    get() : String?{
        return formatter.format(this.date)
    }
}