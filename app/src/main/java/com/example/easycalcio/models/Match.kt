package com.example.easycalcio.models

import android.location.Address
import android.location.Geocoder
import android.location.Location
import java.text.SimpleDateFormat
import java.util.*

class Match(
    val id: Long,
    val title: String,
    val username: String,
    val date: Date,
    val address: String,
    val city: String,
    val playersNumber: Int,
    val players: MutableList<String>
) {

    private val formatter = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.US)


    val formattedDate: String?
        get() : String? {
            return formatter.format(this.date)
        }
}