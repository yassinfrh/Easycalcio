package com.example.easycalcio.models

import android.media.Image
import java.util.*


class User(val id : String,
           val name : String,
           val surname : String,
           val birthday: Date,
           val city : String,
           val role : String,
           val friends : List<User>,
           val matches : List<Match>,
           val profileImage : Image) {

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