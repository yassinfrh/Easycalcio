package com.example.easycalcio.models


class User(val id : String, val name : String, val surname : String, val city : String, val role : String, val friends : List<User>, val matches : List<Match>) {



}