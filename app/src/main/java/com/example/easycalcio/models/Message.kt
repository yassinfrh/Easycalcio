package com.example.easycalcio.models

import java.text.SimpleDateFormat
import java.util.*

class Message() {
    private val formatter = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.US)
    var sender : String = "*"
    var text : String = "*"
    var date : Date = Date()

    constructor(sender: String, text: String, date: Date): this(){
        this.sender = sender
        this.text = text
        this.date = date
    }

    val formattedDate : String?
        get() : String?{
            return formatter.format(this.date)
        }

}