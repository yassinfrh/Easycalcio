package com.example.easycalcio.models

class Chat() {
    var id: Long = 0
    var user1: String = "*"
    var user2: String = "*"
    var messages: MutableList<Message>? = mutableListOf()

    constructor(id: Long, user1: String, user2: String, messages: MutableList<Message>?) : this() {
        this.user1 = user1
        this.user2 = user2
        this.messages = messages
    }
}