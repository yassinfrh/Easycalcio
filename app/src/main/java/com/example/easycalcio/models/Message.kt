package com.example.easycalcio.models

import java.text.SimpleDateFormat
import java.util.*

class Message(val sender : User, val receiver : User, val text : String, val date : Date) {
    private val formatter = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.US)


    val formattedDate : String?
        get() : String?{
            return formatter.format(this.date)
        }

}