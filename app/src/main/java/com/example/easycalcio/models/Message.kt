package com.example.easycalcio.models

import java.text.SimpleDateFormat
import java.util.*

class Message() {
    private val formatter = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.US)
    var sender : String = "*"
    var text : String = "*"
    var date : Date = Date()
    var read : Boolean = false

    constructor(sender: String, text: String, date: Date, read: Boolean): this(){
        this.sender = sender
        this.text = text
        this.date = date
        this.read = read
    }

    val formattedDate : String?
        get() : String?{
            return formatter.format(this.date)
        }

}