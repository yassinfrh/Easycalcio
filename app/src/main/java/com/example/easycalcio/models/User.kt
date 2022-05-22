package com.example.easycalcio.models

import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*


class User() {
    var username: String = "*"
    var name: String = "*"
    var surname: String = "*"
    var birthday: Date = Date()
    var city: String = "*"
    var role: String = "*"
    var friends: MutableList<String> = mutableListOf()
    var matches: MutableList<Long> = mutableListOf()


    constructor(
        username: String,
        name: String,
        surname: String,
        birthday: Date,
        city: String,
        role: String,
        friends: MutableList<String>,
        matches: MutableList<Long>
    ) : this() {
        this.username = username
        this.name = name
        this.surname = surname
        this.birthday = birthday
        this.city = city
        this.role = role
        this.friends = friends
        this.matches = matches
    }

    fun getAge(): Int {
        val dob = Calendar.getInstance()
        val today = Calendar.getInstance()

        dob.timeInMillis = birthday.time

        var age = today[Calendar.YEAR] - dob[Calendar.YEAR]

        if (today[Calendar.DAY_OF_YEAR] < dob[Calendar.DAY_OF_YEAR]) {
            age--
        }

        return age
    }

    private val formatter = SimpleDateFormat("yyyy/MM/dd", Locale.US)


    val formattedDate: String?
        get() : String? {
            return formatter.format(this.birthday)
        }

}