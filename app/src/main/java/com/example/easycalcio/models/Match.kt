package com.example.easycalcio.models

import android.location.Address
import android.location.Geocoder
import android.location.Location
import java.text.SimpleDateFormat
import java.util.*

class Match() {
    var id: Long = 0
    var title: String = "*"
    var username: String = "*"
    var date: Date = Date()
    var address: String = "*"
    var city: String = "*"
    var playersNumber: Int = 0
    var players: MutableList<String>? = mutableListOf()

    constructor(
        id: Long,
        title: String,
        username: String,
        date: Date,
        address: String,
        city: String,
        playersNumber: Int,
        players: MutableList<String>
    ) : this() {
        this.id = id
        this.title = title
        this.username = username
        this.date = date
        this.address = address
        this.city = city
        this.playersNumber = playersNumber
        this.players = players
    }

    private val formatter = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.US)


    val formattedDate: String?
        get() : String? {
            return formatter.format(this.date)
        }
}