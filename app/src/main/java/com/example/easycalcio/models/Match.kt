package com.example.easycalcio.models

import android.location.Address
import android.location.Geocoder
import android.location.Location
import java.text.SimpleDateFormat
import java.util.*

class Match (val id : Long, val title : String, val userId : Long, val date: Date, val location : Location, val players : List<User>){

    private val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)

    val formattedDate : String?
    get() : String?{
        return formatter.format(this.date)
    }
}