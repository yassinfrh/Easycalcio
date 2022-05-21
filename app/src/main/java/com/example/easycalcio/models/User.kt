package com.example.easycalcio.models

import java.time.LocalDate
import java.util.*


class User(
    val username: String,
    val name: String,
    val surname: String,
    val birthday: Date,
    val city: String,
    val role: String,
    val friends: MutableList<User>,
    val matches: MutableList<Match>
) {

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

}