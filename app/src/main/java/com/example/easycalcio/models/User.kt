package com.example.easycalcio.models

import android.location.Location

class User(val id : Long, val name : String, val surname : String, val city : Location, val friends : List<User>, val matches : List<Match>) {



}