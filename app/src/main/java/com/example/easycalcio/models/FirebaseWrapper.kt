package com.example.easycalcio.models

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.easycalcio.activities.MainActivity
import com.example.easycalcio.activities.RegistrationActivity
import com.example.easycalcio.activities.SplashActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.OnProgressListener
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.util.*
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class FirebaseAuthWrapper(private val context: Context) {
    private val TAG: String = FirebaseAuthWrapper::class.simpleName.toString()
    private var auth: FirebaseAuth = Firebase.auth

    fun isAuthenticated(): Boolean {
        return auth.currentUser != null
    }

    fun getUid(): String? {
        return auth.currentUser?.uid
    }

    fun isCompleted(): Boolean {
        val lock = ReentrantLock()
        val condition = lock.newCondition()
        var isCompleted = false

        GlobalScope.launch {
            FirebaseDbWrapper(context).readDbData(object :
                FirebaseDbWrapper.Companion.FirebaseReadCallback {
                override fun onDataChangeCallback(snapshot: DataSnapshot) {
                    Log.d("onDataChangeCallback", "invoked")
                    isCompleted = !snapshot.child("notCompletedUsers").hasChild(getUid()!!)
                    lock.withLock {
                        condition.signal()
                    }
                }

                override fun onCancelledCallback(error: DatabaseError) {
                    Log.d("onCancelledCallback", "invoked")
                }

            })
        }

        lock.withLock {
            condition.await()
        }

        return isCompleted
    }

    fun signUp(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d(TAG, "createUserWithEmail:success")

                //add not completely registered to database
                val firebaseDbWrapper = FirebaseDbWrapper(context)
                firebaseDbWrapper.writeNotCompletedUser()

                val intent = Intent(this.context, RegistrationActivity::class.java)
                context.startActivity(intent)
                val activity = context as AppCompatActivity
                activity.finish()
            } else {
                // If sign in fails, display a message to the user.
                Log.w(TAG, "createUserWithEmail:failure", task.exception)
                Toast.makeText(
                    context,
                    "Sign-up failed. Error message: ${task.exception!!.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    fun signIn(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithEmail:success")
                    val intent = Intent(this.context, SplashActivity::class.java)
                    context.startActivity(intent)
                    val activity = context as AppCompatActivity
                    activity.finish()
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithEmail:failure", task.exception)
                    Toast.makeText(context, "Authentication failed.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    fun signOut() {
        auth.signOut()
        val intent = Intent(context, SplashActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        intent.putExtra("EXIT", true)
        context.startActivity(intent)
    }

}

fun alreadyUsedUsername(context: Context, username: String): Boolean {
    val lock = ReentrantLock()
    val condition = lock.newCondition()
    var used = false

    GlobalScope.launch {
        FirebaseDbWrapper(context).readDbData(object :
            FirebaseDbWrapper.Companion.FirebaseReadCallback {
            override fun onDataChangeCallback(snapshot: DataSnapshot) {
                Log.d("onDataChangeCallback", "invoked")
                for (child in snapshot.child("users").children) {
                    val user: User = child.getValue(User::class.java)!!
                    if (user.username == username) {
                        used = true
                    }
                }
                lock.withLock {
                    condition.signal()
                }
            }

            override fun onCancelledCallback(error: DatabaseError) {
                Log.d("onCancelledCallback", "invoked")
            }

        })
    }
    lock.withLock {
        condition.await()
    }
    return used
}

fun registerUser(context: Context) {
    val uid = FirebaseAuthWrapper(context).getUid()!!
    val lock = ReentrantLock()
    val condition = lock.newCondition()

    GlobalScope.launch {
        FirebaseDbWrapper(context).readDbData(object :
            FirebaseDbWrapper.Companion.FirebaseReadCallback {
            override fun onDataChangeCallback(snapshot: DataSnapshot) {
                snapshot.child("notCompletedUsers")
                    .child(uid).ref.removeValue()
                lock.withLock {
                    condition.signal()
                }
            }

            override fun onCancelledCallback(error: DatabaseError) {
                Log.d("onCancelledCallback", "invoked")
            }

        })
    }

}

fun getUser(context: Context): User {
    val uid = FirebaseAuthWrapper(context).getUid()
    val lock = ReentrantLock()
    val condition = lock.newCondition()
    var user: User? = null

    GlobalScope.launch {
        FirebaseDbWrapper(context).readDbData(object :
            FirebaseDbWrapper.Companion.FirebaseReadCallback {
            override fun onDataChangeCallback(snapshot: DataSnapshot) {
                Log.d("onDataChangeCallback", "invoked")
                user = snapshot.child("users").child(uid!!).getValue(User::class.java)

                lock.withLock {
                    condition.signal()
                }
            }

            override fun onCancelledCallback(error: DatabaseError) {
                Log.d("onCancelledCallback", "invoked")
            }

        })
    }

    lock.withLock {
        condition.await()
    }

    return user!!
}

fun getUsersStartingWith(context: Context, query: String): MutableList<User>? {
    val uid = FirebaseAuthWrapper(context).getUid()
    val lock = ReentrantLock()
    val condition = lock.newCondition()
    var list: MutableList<User>? = null

    GlobalScope.launch {
        FirebaseDbWrapper(context).readDbData(object :
            FirebaseDbWrapper.Companion.FirebaseReadCallback {
            override fun onDataChangeCallback(snapshot: DataSnapshot) {
                val children = snapshot.child("users").children
                for (child in children) {
                    val user = child.getValue(User::class.java)
                    if (user!!.username.startsWith(query, true) && child.key != uid) {
                        if (list == null) {
                            list = mutableListOf(user)
                        } else {
                            var contains = false
                            for (element in list!!) {
                                if (element.username == user.username) {
                                    contains = true
                                    break
                                }
                            }
                            if (!contains) {
                                list!!.add(user)
                            }
                        }
                    }
                }
                lock.withLock {
                    condition.signal()
                }
            }

            override fun onCancelledCallback(error: DatabaseError) {
            }

        })
    }
    lock.withLock {
        condition.await()
    }
    return list
}

fun getUserWithUsername(context: Context, username: String): User {
    val lock = ReentrantLock()
    val condition = lock.newCondition()
    var user: User? = null

    GlobalScope.launch {
        FirebaseDbWrapper(context).readDbData(object :
            FirebaseDbWrapper.Companion.FirebaseReadCallback {
            override fun onDataChangeCallback(snapshot: DataSnapshot) {
                val children = snapshot.child("users").children
                for (child in children) {
                    val curr = child.getValue(User::class.java)
                    if (curr!!.username == username) {
                        user = curr
                        break
                    }
                }
                lock.withLock {
                    condition.signal()
                }
            }

            override fun onCancelledCallback(error: DatabaseError) {
            }

        })

    }
    lock.withLock {
        condition.await()
    }
    return user!!
}

fun sendFriendRequest(context: Context, username: String) {
    val uid = FirebaseAuthWrapper(context).getUid()!!
    val lock = ReentrantLock()
    val condition = lock.newCondition()
    var requests: MutableList<String>? = null

    GlobalScope.launch {
        FirebaseDbWrapper(context).readDbData(object :
            FirebaseDbWrapper.Companion.FirebaseReadCallback {
            override fun onDataChangeCallback(snapshot: DataSnapshot) {
                requests = snapshot.child("friendRequests").child(uid).value as MutableList<String>?

                lock.withLock {
                    condition.signal()
                }
            }

            override fun onCancelledCallback(error: DatabaseError) {
            }

        })

    }
    lock.withLock {
        condition.await()
    }

    if (requests == null) {
        requests = mutableListOf(username)
    } else {
        requests!!.add(username)
    }

    FirebaseDbWrapper(context).dbRef.child("friendRequests").child(uid).setValue(requests)
}

fun hasSentRequest(context: Context, username: String): Boolean {

    val uid = FirebaseAuthWrapper(context).getUid()!!
    val lock = ReentrantLock()
    val condition = lock.newCondition()
    var hasSent = false

    GlobalScope.launch {
        FirebaseDbWrapper(context).readDbData(object :
            FirebaseDbWrapper.Companion.FirebaseReadCallback {
            override fun onDataChangeCallback(snapshot: DataSnapshot) {
                val children = snapshot.child("friendRequests").child(uid).children
                for (child in children) {
                    val curr = child.getValue(String::class.java)
                    if (curr!! == username) {
                        hasSent = true
                        break
                    }
                }
                lock.withLock {
                    condition.signal()
                }
            }

            override fun onCancelledCallback(error: DatabaseError) {
            }

        })

    }

    lock.withLock {
        condition.await()
    }
    return hasSent
}

fun getReceivedRequests(context: Context): MutableList<User>? {
    val uid = FirebaseAuthWrapper(context).getUid()

    val lock = ReentrantLock()
    var condition = lock.newCondition()
    var list: MutableList<String>? = null
    var usersList: MutableList<User>? = null
    var loggedUser: User? = null

    GlobalScope.launch {
        FirebaseDbWrapper(context).readDbData(object :
            FirebaseDbWrapper.Companion.FirebaseReadCallback {
            override fun onDataChangeCallback(snapshot: DataSnapshot) {
                Log.d("onDataChangeCallback", "invoked")
                loggedUser = snapshot.child("users").child(uid!!).getValue(User::class.java)

                lock.withLock {
                    condition.signal()
                }
            }

            override fun onCancelledCallback(error: DatabaseError) {
                Log.d("onCancelledCallback", "invoked")
            }

        })
    }

    lock.withLock {
        condition.await()
    }

    val username = loggedUser!!.username

    condition = lock.newCondition()
    //get list of users that sent me a request (uid)
    GlobalScope.launch {
        FirebaseDbWrapper(context).readDbData(object :
            FirebaseDbWrapper.Companion.FirebaseReadCallback {
            override fun onDataChangeCallback(snapshot: DataSnapshot) {
                val children = snapshot.child("friendRequests").children
                for (child in children) {
                    val currRequests = child.value as List<String>
                    for (request in currRequests) {
                        if (request == username) {
                            if (list == null) {
                                list = mutableListOf(child.key!!)
                            } else {
                                list!!.add(child.key!!)
                            }
                        }
                    }
                }
                lock.withLock {
                    condition.signal()
                }
            }

            override fun onCancelledCallback(error: DatabaseError) {
            }

        })

    }

    lock.withLock {
        condition.await()
    }

    //translate uid to user
    if (list != null) {
        condition = lock.newCondition()

        GlobalScope.launch {
            FirebaseDbWrapper(context).readDbData(object :
                FirebaseDbWrapper.Companion.FirebaseReadCallback {
                override fun onDataChangeCallback(snapshot: DataSnapshot) {
                    val children = snapshot.child("users").children
                    for (child in children) {
                        if (list!!.contains(child.key)) {
                            val user = child.getValue(User::class.java)
                            if (usersList == null) {
                                usersList = mutableListOf(user!!)
                            } else {
                                usersList!!.add(user!!)
                            }
                        }
                    }
                    lock.withLock {
                        condition.signal()
                    }
                }

                override fun onCancelledCallback(error: DatabaseError) {
                }

            })

        }

        lock.withLock {
            condition.await()
        }
    }

    return usersList
}

fun acceptFriendRequest(context: Context, sender: String) {
    val loggedUserUid = FirebaseAuthWrapper(context).getUid()!!
    val lock1 = ReentrantLock()
    val condition1 = lock1.newCondition()
    var senderUid: String? = null

    GlobalScope.launch {
        FirebaseDbWrapper(context).readDbData(object :
            FirebaseDbWrapper.Companion.FirebaseReadCallback {
            override fun onDataChangeCallback(snapshot: DataSnapshot) {
                val children = snapshot.child("users").children
                for (child in children) {
                    val currUser = child.getValue(User::class.java)
                    //get sender uid
                    if (currUser!!.username == sender) {
                        senderUid = child.key
                        break
                    }
                }
                lock1.withLock {
                    condition1.signal()
                }
            }

            override fun onCancelledCallback(error: DatabaseError) {
            }

        })

    }
    lock1.withLock {
        condition1.await()
    }

    val lock2 = ReentrantLock()
    val condition2 = lock2.newCondition()
    var loggedUser: User? = null

    GlobalScope.launch {
        //get logged user username
        FirebaseDbWrapper(context).readDbData(object :
            FirebaseDbWrapper.Companion.FirebaseReadCallback {
            override fun onDataChangeCallback(snapshot: DataSnapshot) {
                Log.d("onDataChangeCallback", "invoked")
                loggedUser = snapshot.child("users").child(loggedUserUid).getValue(User::class.java)

                lock2.withLock {
                    condition2.signal()
                }
            }

            override fun onCancelledCallback(error: DatabaseError) {
                Log.d("onCancelledCallback", "invoked")
            }

        })
    }
    lock2.withLock {
        condition2.await()
    }
    val loggedUsername = loggedUser!!.username

    val lock3 = ReentrantLock()
    val condition3 = lock3.newCondition()
    var requestsList: MutableList<String>? = null

    GlobalScope.launch {
        FirebaseDbWrapper(context).readDbData(object :
            FirebaseDbWrapper.Companion.FirebaseReadCallback {
            override fun onDataChangeCallback(snapshot: DataSnapshot) {
                requestsList =
                    snapshot.child("friendRequests")
                        .child(senderUid!!).value as MutableList<String>?

                lock3.withLock {
                    condition3.signal()
                }
            }

            override fun onCancelledCallback(error: DatabaseError) {
            }

        })

    }
    lock3.withLock {
        condition3.await()
    }

    //remove from requests list
    requestsList!!.remove(loggedUsername)
    val db = FirebaseDbWrapper(context).dbRef
    db.child("friendRequests").child(senderUid!!).setValue(requestsList)


    val lock4 = ReentrantLock()
    val condition4 = lock4.newCondition()
    var senderUser: User? = null

    GlobalScope.launch {
        FirebaseDbWrapper(context).readDbData(object :
            FirebaseDbWrapper.Companion.FirebaseReadCallback {
            override fun onDataChangeCallback(snapshot: DataSnapshot) {
                val children = snapshot.child("users").children
                for (child in children) {
                    val curr = child.getValue(User::class.java)
                    if (curr!!.username == sender) {
                        senderUser = curr
                        break
                    }
                }
                lock4.withLock {
                    condition4.signal()
                }
            }

            override fun onCancelledCallback(error: DatabaseError) {
            }

        })
    }
    lock4.withLock {
        condition4.await()
    }
    //add to user friends list
    if (senderUser!!.friends == null) {
        senderUser!!.friends = mutableListOf(loggedUsername)
    } else {
        senderUser!!.friends!!.add(loggedUsername)
    }

    db.child("users").child(senderUid!!).setValue(senderUser)


    //add to current logged user friends list
    if (loggedUser!!.friends == null) {
        loggedUser!!.friends = mutableListOf(sender)
    } else {
        loggedUser!!.friends!!.add(sender)
    }
    db.child("users").child(loggedUserUid).setValue(loggedUser)
}

fun declineFriendRequest(context: Context, sender: String) {
    val loggedUserUid = FirebaseAuthWrapper(context).getUid()!!
    val lock1 = ReentrantLock()
    val condition1 = lock1.newCondition()
    var senderUid: String? = null

    GlobalScope.launch {
        FirebaseDbWrapper(context).readDbData(object :
            FirebaseDbWrapper.Companion.FirebaseReadCallback {
            override fun onDataChangeCallback(snapshot: DataSnapshot) {
                val children = snapshot.child("users").children
                for (child in children) {
                    val currUser = child.getValue(User::class.java)
                    //get sender uid
                    if (currUser!!.username == sender) {
                        senderUid = child.key
                        break
                    }
                }
                lock1.withLock {
                    condition1.signal()
                }
            }

            override fun onCancelledCallback(error: DatabaseError) {
            }

        })

    }
    lock1.withLock {
        condition1.await()
    }

    val lock2 = ReentrantLock()
    val condition2 = lock2.newCondition()
    var loggedUser: User? = null

    GlobalScope.launch {
        //get logged user username
        FirebaseDbWrapper(context).readDbData(object :
            FirebaseDbWrapper.Companion.FirebaseReadCallback {
            override fun onDataChangeCallback(snapshot: DataSnapshot) {
                Log.d("onDataChangeCallback", "invoked")
                loggedUser = snapshot.child("users").child(loggedUserUid).getValue(User::class.java)

                lock2.withLock {
                    condition2.signal()
                }
            }

            override fun onCancelledCallback(error: DatabaseError) {
                Log.d("onCancelledCallback", "invoked")
            }

        })
    }
    lock2.withLock {
        condition2.await()
    }
    val loggedUsername = loggedUser!!.username

    val lock3 = ReentrantLock()
    val condition3 = lock3.newCondition()
    var requestsList: MutableList<String>? = null

    GlobalScope.launch {
        FirebaseDbWrapper(context).readDbData(object :
            FirebaseDbWrapper.Companion.FirebaseReadCallback {
            override fun onDataChangeCallback(snapshot: DataSnapshot) {
                requestsList =
                    snapshot.child("friendRequests")
                        .child(senderUid!!).value as MutableList<String>?

                lock3.withLock {
                    condition3.signal()
                }
            }

            override fun onCancelledCallback(error: DatabaseError) {
            }

        })

    }
    lock3.withLock {
        condition3.await()
    }

    //remove from requests list
    requestsList!!.remove(loggedUsername)
    val db = FirebaseDbWrapper(context).dbRef
    db.child("friendRequests").child(senderUid!!).setValue(requestsList)
}

fun removeFriend(context: Context, friendUsername: String) {

    val db = FirebaseDbWrapper(context).dbRef
    val loggedUserUid = FirebaseAuthWrapper(context).getUid()!!
    val lock1 = ReentrantLock()
    val condition1 = lock1.newCondition()

    var loggedUser: User? = null

    //get current logged user
    GlobalScope.launch {
        loggedUser = getUser(context)
        lock1.withLock {
            condition1.signal()
        }
    }
    lock1.withLock {
        condition1.await()
    }

    //remove user from current logged user friend list
    loggedUser!!.friends!!.remove(friendUsername)
    db.child("users").child(loggedUserUid).setValue(loggedUser)

    //get friend user and uid
    val lock2 = ReentrantLock()
    val condition2 = lock2.newCondition()
    var friendUser: User? = null
    var friendUserUid: String? = null
    GlobalScope.launch {
        FirebaseDbWrapper(context).readDbData(object :
            FirebaseDbWrapper.Companion.FirebaseReadCallback {
            override fun onDataChangeCallback(snapshot: DataSnapshot) {
                val children = snapshot.child("users").children
                for (child in children) {
                    val curr = child.getValue(User::class.java)
                    if (curr!!.username == friendUsername) {
                        friendUser = curr
                        friendUserUid = child.key
                        break
                    }
                }

                lock2.withLock {
                    condition2.signal()
                }
            }

            override fun onCancelledCallback(error: DatabaseError) {
            }

        })
    }
    lock2.withLock {
        condition2.await()
    }

    //remove current logged user from friend friend list
    friendUser!!.friends!!.remove(loggedUser!!.username)
    db.child("users").child(friendUserUid!!).setValue(friendUser)
}

fun getFriendsList(context: Context): MutableList<User>? {

    val lock1 = ReentrantLock()
    val condition1 = lock1.newCondition()

    var friendsUsernames: MutableList<String>? = null
    GlobalScope.launch {
        friendsUsernames = getUser(context).friends
        lock1.withLock {
            condition1.signal()
        }
    }
    lock1.withLock {
        condition1.await()
    }
    if (friendsUsernames == null) {
        return null
    }

    val lock2 = ReentrantLock()
    val condition2 = lock2.newCondition()

    var friends: MutableList<User>? = null
    GlobalScope.launch {
        FirebaseDbWrapper(context).readDbData(object :
            FirebaseDbWrapper.Companion.FirebaseReadCallback {
            override fun onDataChangeCallback(snapshot: DataSnapshot) {
                val children = snapshot.child("users").children
                for (child in children) {
                    for (username in friendsUsernames!!) {
                        val user = child.getValue(User::class.java)
                        if (username == user!!.username) {
                            if (friends == null) {
                                friends = mutableListOf(user)
                            } else {
                                var contains = false
                                for (element in friends!!) {
                                    if (element.username == user.username) {
                                        contains = true
                                        break
                                    }
                                }
                                if (!contains) {
                                    friends!!.add(user)
                                }
                            }
                        }
                    }
                }

                lock2.withLock {
                    condition2.signal()
                }
            }

            override fun onCancelledCallback(error: DatabaseError) {
            }

        })
    }
    lock2.withLock {
        condition2.await()
    }

    return friends
}

fun sendMessage(message: Message, receiver: String, chatId: Long, context: Context) {

    val lock = ReentrantLock()
    val condition = lock.newCondition()
    var chat: Chat? = null
    GlobalScope.launch {
        FirebaseDbWrapper(context).readDbData(object :
            FirebaseDbWrapper.Companion.FirebaseReadCallback {
            override fun onDataChangeCallback(snapshot: DataSnapshot) {
                chat =
                    snapshot.child("chats").child(chatId.toString()).getValue(Chat::class.java)

                lock.withLock {
                    condition.signal()
                }
            }

            override fun onCancelledCallback(error: DatabaseError) {
            }

        })
    }
    lock.withLock {
        condition.await()
    }

    if (chat!!.messages == null) {
        chat!!.messages = mutableListOf(message)
    } else {
        chat!!.messages!!.add(message)
    }

    FirebaseDbWrapper(context).dbRef.child("chats").child(chatId.toString()).setValue(chat)

}

fun readMessages(context: Context, chatId: Long) {
    val lock = ReentrantLock()
    val condition = lock.newCondition()
    var chat: Chat? = null
    var user: User? = null
    GlobalScope.launch {
        user = getUser(context)
        FirebaseDbWrapper(context).readDbData(object :
            FirebaseDbWrapper.Companion.FirebaseReadCallback {
            override fun onDataChangeCallback(snapshot: DataSnapshot) {

                chat = snapshot.child("chats").child(chatId.toString()).getValue(Chat::class.java)

                lock.withLock {
                    condition.signal()
                }
            }

            override fun onCancelledCallback(error: DatabaseError) {
            }

        })
    }
    lock.withLock {
        condition.await()
    }

    if (chat!!.messages != null) {
        for (message in chat!!.messages!!) {
            if (message.sender != user!!.username) {
                message.read = true
            }
        }
    }

    FirebaseDbWrapper(context).dbRef.child("chats").child(chatId.toString()).setValue(chat)
}

fun getChatId(user1: String, user2: String, context: Context): Long {
    val lock1 = ReentrantLock()
    val condition1 = lock1.newCondition()
    var chatId: Long? = null

    GlobalScope.launch {
        FirebaseDbWrapper(context).readDbData(object :
            FirebaseDbWrapper.Companion.FirebaseReadCallback {
            override fun onDataChangeCallback(snapshot: DataSnapshot) {
                val children = snapshot.child("chats").children
                for (child in children) {
                    val chat = child.getValue(Chat::class.java)
                    if ((chat!!.user1 == user1 || chat.user1 == user2) && (chat.user2 == user1 || chat.user2 == user2)) {
                        chatId = child.key.toString().toLong()
                        break
                    }
                }
                lock1.withLock {
                    condition1.signal()
                }
            }

            override fun onCancelledCallback(error: DatabaseError) {
            }

        })
    }
    lock1.withLock {
        condition1.await()
    }

    if (chatId == null) {
        chatId = 0.toLong()
        val lock2 = ReentrantLock()
        val condition2 = lock2.newCondition()
        GlobalScope.launch {
            FirebaseDbWrapper(context).readDbData(object :
                FirebaseDbWrapper.Companion.FirebaseReadCallback {
                override fun onDataChangeCallback(snapshot: DataSnapshot) {
                    val children = snapshot.child("chats").children
                    for (child in children) {
                        val id = child.key.toString().toLong()
                        if (id > chatId!!) {
                            chatId = id
                        }
                    }
                    chatId = chatId!! + 1

                    lock2.withLock {
                        condition2.signal()
                    }
                }

                override fun onCancelledCallback(error: DatabaseError) {
                }

            })
        }
        lock2.withLock {
            condition2.await()
        }
        val newChat = Chat(chatId!!, user1, user2, mutableListOf())
        FirebaseDbWrapper(context).dbRef.child("chats").child(chatId.toString())
            .setValue(newChat)
    }

    return chatId!!
}

fun getMatchId(context: Context): Long {
    val lock1 = ReentrantLock()
    val condition1 = lock1.newCondition()
    var matchId: Long = 0
    GlobalScope.launch {
        FirebaseDbWrapper(context).readDbData(object :
            FirebaseDbWrapper.Companion.FirebaseReadCallback {
            override fun onDataChangeCallback(snapshot: DataSnapshot) {
                val children = snapshot.child("matches").children
                for (child in children) {
                    if (child.key!!.toLong() > matchId) {
                        matchId = child.key!!.toLong()
                    }
                }
                lock1.withLock {
                    condition1.signal()
                }
            }

            override fun onCancelledCallback(error: DatabaseError) {
            }

        })
    }
    lock1.withLock {
        condition1.await()
    }
    matchId++
    return matchId
}

fun createMatch(context: Context, match: Match, friends: MutableList<String>?) {
    val dbRef = FirebaseDbWrapper(context).dbRef
    val uid = FirebaseAuthWrapper(context).getUid()
    val lock1 = ReentrantLock()
    val condition1 = lock1.newCondition()
    var currUser: User? = null

    GlobalScope.launch {
        currUser = getUser(context)
        lock1.withLock {
            condition1.signal()
        }
    }
    lock1.withLock {
        condition1.await()
    }

    //add match to current user matches list
    if (currUser!!.matches == null) {
        currUser!!.matches = mutableListOf(match.id)
    } else {
        currUser!!.matches!!.add(match.id)
    }
    dbRef.child("users").child(uid!!).setValue(currUser)

    //send match requests
    if (friends != null) {
        dbRef.child("matchRequests").child(match.id.toString()).setValue(friends)
    }

    //create match
    dbRef.child("matches").child(match.id.toString()).setValue(match)
}

fun getMatches(context: Context): MutableList<Match>? {
    val lock1 = ReentrantLock()
    val condition1 = lock1.newCondition()
    var matchIdList: MutableList<Long>? = null

    GlobalScope.launch {
        val user = getUser(context)
        matchIdList = user.matches
        lock1.withLock {
            condition1.signal()
        }
    }
    lock1.withLock {
        condition1.await()
    }
    if (matchIdList == null) {
        return null
    }

    val lock2 = ReentrantLock()
    val condition2 = lock2.newCondition()
    var matchList: MutableList<Match>? = null

    GlobalScope.launch {
        FirebaseDbWrapper(context).readDbData(object :
            FirebaseDbWrapper.Companion.FirebaseReadCallback {
            override fun onDataChangeCallback(snapshot: DataSnapshot) {
                val children = snapshot.child("matches").children
                for (child in children) {
                    for (id in matchIdList!!) {
                        if (id.toString() == child.key) {
                            val match = child.getValue(Match::class.java)
                            if (match!!.date > Date()) {
                                if (matchList == null) {
                                    matchList = mutableListOf(match)
                                } else {
                                    matchList!!.add(match)
                                }
                            }
                        }
                    }
                }
                lock2.withLock {
                    condition2.signal()
                }
            }

            override fun onCancelledCallback(error: DatabaseError) {
            }

        })
    }

    lock2.withLock {
        condition2.await()
    }

    return matchList

}

fun getMatchRequests(context: Context): MutableList<Match>? {
    val lock = ReentrantLock()
    val condition = lock.newCondition()
    var username: String? = null

    GlobalScope.launch {
        val user = getUser(context)
        username = user.username
        lock.withLock {
            condition.signal()
        }
    }
    lock.withLock {
        condition.await()
    }

    val lock1 = ReentrantLock()
    val condition1 = lock1.newCondition()
    var matchIdList: MutableList<String>? = null

    GlobalScope.launch {
        FirebaseDbWrapper(context).readDbData(object :
            FirebaseDbWrapper.Companion.FirebaseReadCallback {
            override fun onDataChangeCallback(snapshot: DataSnapshot) {
                val children = snapshot.child("matchRequests").children
                for (child in children) {
                    for (user in child.children) {
                        if (user.value == username) {
                            if (matchIdList == null) {
                                matchIdList = mutableListOf(child.key!!)
                            } else {
                                matchIdList!!.add(child.key!!)
                            }
                        }
                    }
                }
                lock1.withLock {
                    condition1.signal()
                }
            }

            override fun onCancelledCallback(error: DatabaseError) {
            }

        })
    }
    lock1.withLock {
        condition1.await()
    }

    if (matchIdList == null) {
        return null
    }

    val lock2 = ReentrantLock()
    val condition2 = lock2.newCondition()
    var matchList: MutableList<Match>? = null

    GlobalScope.launch {
        FirebaseDbWrapper(context).readDbData(object :
            FirebaseDbWrapper.Companion.FirebaseReadCallback {
            override fun onDataChangeCallback(snapshot: DataSnapshot) {
                val children = snapshot.child("matches").children
                for (child in children) {
                    for (id in matchIdList!!) {
                        if (id == child.key) {
                            val match = child.getValue(Match::class.java)
                            if (matchList == null) {
                                matchList = mutableListOf(match!!)
                            } else {
                                matchList!!.add(match!!)
                            }
                        }
                    }
                }
                lock2.withLock {
                    condition2.signal()
                }
            }

            override fun onCancelledCallback(error: DatabaseError) {
            }

        })
    }

    lock2.withLock {
        condition2.await()
    }

    return matchList
}

fun getMatch(context: Context, matchId: Long): Match {
    val lock = ReentrantLock()
    val condition = lock.newCondition()
    var match: Match? = null

    GlobalScope.launch {
        FirebaseDbWrapper(context).readDbData(object :
            FirebaseDbWrapper.Companion.FirebaseReadCallback {
            override fun onDataChangeCallback(snapshot: DataSnapshot) {

                match =
                    snapshot.child("matches").child(matchId.toString()).getValue(Match::class.java)

                lock.withLock {
                    condition.signal()
                }
            }

            override fun onCancelledCallback(error: DatabaseError) {
            }

        })
    }
    lock.withLock {
        condition.await()
    }

    return match!!
}

fun removeMatch(context: Context, matchId: Long) {
    val lock = ReentrantLock()
    val condition = lock.newCondition()
    var modifiedUsers : MutableList<User>? = null

    GlobalScope.launch {
        FirebaseDbWrapper(context).readDbData(object :
            FirebaseDbWrapper.Companion.FirebaseReadCallback {
            override fun onDataChangeCallback(snapshot: DataSnapshot) {

                snapshot.child("matchRequests").child(matchId.toString()).ref.removeValue()
                snapshot.child("matches").child(matchId.toString()).ref.removeValue()

                for (child in snapshot.child("users").children) {
                    val user = child.getValue(User::class.java)
                    if (user!!.matches!!.contains(matchId)) {
                        user.matches!!.remove(matchId)
                        if(modifiedUsers == null){
                            modifiedUsers = mutableListOf(user)
                        }
                        else{
                            modifiedUsers!!.add(user)
                        }
                    }
                }

                lock.withLock {
                    condition.signal()
                }
            }

            override fun onCancelledCallback(error: DatabaseError) {
            }

        })
    }
    lock.withLock {
        condition.await()
    }

    if(modifiedUsers != null){
        for(user in modifiedUsers!!){
            GlobalScope.launch {
                replaceUser(context, user.username, user)
            }
        }
    }
}

fun editMatch(context: Context, match: Match, friends: MutableList<String>?) {
    val lock = ReentrantLock()
    val condition = lock.newCondition()
    var currUser: User? = null

    GlobalScope.launch {
        currUser = getUser(context)
        lock.withLock {
            condition.signal()
        }
    }

    lock.withLock {
        condition.await()
    }

    var removedPlayers: MutableList<String>? = null

    if (friends == null) {
        removedPlayers = match.players
        removedPlayers!!.remove(currUser!!.username)
        match.players = mutableListOf(currUser!!.username)
    } else {
        for (player in match.players!!) {
            if (!friends.contains(player) && player != currUser!!.username) {
                match.players!!.remove(player)
                if (removedPlayers == null) {
                    removedPlayers = mutableListOf(player)
                } else {
                    removedPlayers.add(player)
                }
            }
        }
    }


    if (match.players != null) {
        for (player in match.players!!) {
            if (friends != null && friends.contains(player)) {
                friends.remove(player)
            }
        }
    }

    val lock1 = ReentrantLock()
    val condition1 = lock1.newCondition()
    var requests: MutableList<String>? = null
    var removedUsers: MutableMap<String, User>? = null

    GlobalScope.launch {
        FirebaseDbWrapper(context).readDbData(object :
            FirebaseDbWrapper.Companion.FirebaseReadCallback {
            override fun onDataChangeCallback(snapshot: DataSnapshot) {

                requests = snapshot.child("matchRequests")
                    .child(match.id.toString()).value as MutableList<String>?
                if (friends != null) {
                    if (requests == null) {
                        requests = friends
                    } else {
                        requests!!.addAll(friends)
                    }
                }

                if (removedPlayers != null) {
                    val children = snapshot.child("users").children
                    for (child in children) {
                        val userChild = child.getValue(User::class.java)
                        for (player in removedPlayers) {
                            if (userChild!!.username == player) {
                                if (removedUsers == null) {
                                    removedUsers = mutableMapOf(child.key!! to userChild)
                                } else {
                                    removedUsers!![child.key!!] = userChild
                                }
                            }
                        }
                    }
                }

                lock1.withLock {
                    condition1.signal()
                }
            }

            override fun onCancelledCallback(error: DatabaseError) {
            }

        })
    }
    lock1.withLock {
        condition1.await()
    }

    if (requests != null) {
        requests = requests!!.toSet().toMutableList()
        FirebaseDbWrapper(context).dbRef.child("matchRequests").child(match.id.toString())
            .setValue(requests)
    }
    FirebaseDbWrapper(context).dbRef.child("matches").child(match.id.toString()).setValue(match)

    if (removedUsers != null) {
        for (uid in removedUsers!!.keys) {
            val curr = removedUsers!![uid]
            curr!!.matches!!.remove(match.id)
            FirebaseDbWrapper(context).dbRef.child("users").child(uid).setValue(curr)
        }
    }
}

fun acceptMatchRequest(context: Context, matchId: Long) {
    val lock = ReentrantLock()
    val condition = lock.newCondition()
    var match: Match? = null
    var user: User? = null
    val uid = FirebaseAuthWrapper(context).getUid()

    GlobalScope.launch {
        match = getMatch(context, matchId)
        user = getUser(context)
        lock.withLock {
            condition.signal()
        }
    }
    lock.withLock {
        condition.await()
    }

    match!!.players!!.add(user!!.username)

    if (user!!.matches == null) {
        user!!.matches = mutableListOf(matchId)
    } else {
        user!!.matches!!.add(matchId)
    }

    val lock1 = ReentrantLock()
    val condition1 = lock1.newCondition()
    var matchRequests: MutableList<String>? = null

    GlobalScope.launch {
        FirebaseDbWrapper(context).readDbData(object :
            FirebaseDbWrapper.Companion.FirebaseReadCallback {
            override fun onDataChangeCallback(snapshot: DataSnapshot) {

                matchRequests = snapshot.child("matchRequests")
                    .child(matchId.toString()).value as MutableList<String>?

                lock1.withLock {
                    condition1.signal()
                }
            }

            override fun onCancelledCallback(error: DatabaseError) {
            }

        })
    }
    lock1.withLock {
        condition1.await()
    }

    if (matchRequests != null) {
        matchRequests!!.remove(user!!.username)
        FirebaseDbWrapper(context).dbRef.child("matchRequests").child(matchId.toString())
            .setValue(matchRequests)
        FirebaseDbWrapper(context).dbRef.child("users").child(uid!!).setValue(user)
        FirebaseDbWrapper(context).dbRef.child("matches").child(matchId.toString()).setValue(match)
    }
}

fun declineMatchRequest(context: Context, matchId: Long) {
    val lock = ReentrantLock()
    val condition = lock.newCondition()
    var matchRequests: MutableList<String>? = null

    GlobalScope.launch {
        FirebaseDbWrapper(context).readDbData(object :
            FirebaseDbWrapper.Companion.FirebaseReadCallback {
            override fun onDataChangeCallback(snapshot: DataSnapshot) {

                matchRequests = snapshot.child("matchRequests")
                    .child(matchId.toString()).value as MutableList<String>?

                lock.withLock {
                    condition.signal()
                }
            }

            override fun onCancelledCallback(error: DatabaseError) {
            }

        })
    }
    lock.withLock {
        condition.await()
    }

    val lock1 = ReentrantLock()
    val condition1 = lock1.newCondition()
    var user: User? = null

    GlobalScope.launch {
        user = getUser(context)
        lock1.withLock {
            condition1.signal()
        }
    }
    lock1.withLock {
        condition1.await()
    }

    if (matchRequests != null) {
        matchRequests!!.remove(user!!.username)
        FirebaseDbWrapper(context).dbRef.child("matchRequests").child(matchId.toString())
            .setValue(matchRequests)
    }
}

fun quitMatch(context: Context, matchId: Long) {
    val lock = ReentrantLock()
    val condition = lock.newCondition()
    var match: Match? = null
    var user: User? = null
    val uid = FirebaseAuthWrapper(context).getUid()
    GlobalScope.launch {
        match = getMatch(context, matchId)
        user = getUser(context)

        lock.withLock {
            condition.signal()
        }
    }

    lock.withLock {
        condition.await()
    }

    user!!.matches!!.remove(matchId)
    match!!.players!!.remove(user!!.username)

    FirebaseDbWrapper(context).dbRef.child("users").child(uid!!).setValue(user)
    FirebaseDbWrapper(context).dbRef.child("matches").child(matchId.toString()).setValue(match)
}

fun getChats(context: Context): MutableList<Chat>? {
    val lock = ReentrantLock()
    val condition = lock.newCondition()
    var user: User? = null

    GlobalScope.launch {
        user = getUser(context)

        lock.withLock {
            condition.signal()
        }
    }

    lock.withLock {
        condition.await()
    }

    val lock1 = ReentrantLock()
    val condition1 = lock1.newCondition()
    var chats: MutableList<Chat>? = null

    GlobalScope.launch {
        FirebaseDbWrapper(context).readDbData(object :
            FirebaseDbWrapper.Companion.FirebaseReadCallback {
            override fun onDataChangeCallback(snapshot: DataSnapshot) {

                val children = snapshot.child("chats").children
                for (child in children) {
                    val chat = child.getValue(Chat::class.java)
                    if (chat!!.user1 == user!!.username || chat.user2 == user!!.username) {
                        if (chats == null) {
                            chats = mutableListOf(chat)
                        } else {
                            chats!!.add(chat)
                        }
                    }
                }

                lock1.withLock {
                    condition1.signal()
                }
            }

            override fun onCancelledCallback(error: DatabaseError) {
            }

        })
    }
    lock1.withLock {
        condition1.await()
    }
    return chats
}

fun replaceUser(context: Context, oldUsername: String, user: User) {
    val lock = ReentrantLock()
    val condition = lock.newCondition()
    var oldUserUid: String? = null

    GlobalScope.launch {
        FirebaseDbWrapper(context).readDbData(object :
            FirebaseDbWrapper.Companion.FirebaseReadCallback {
            override fun onDataChangeCallback(snapshot: DataSnapshot) {

                val children = snapshot.child("users").children
                for(child in children){
                    val curr = child.getValue(User::class.java)
                    if(curr!!.username == oldUsername){
                        oldUserUid = child.key
                        break
                    }
                }

                lock.withLock {
                    condition.signal()
                }

            }

            override fun onCancelledCallback(error: DatabaseError) {
            }

        })

    }
    lock.withLock {
        condition.await()
    }

    FirebaseDbWrapper(context).dbRef.child("users").child(oldUserUid!!).setValue(user)

}

fun replaceUsernameInMatchRequests(context: Context, oldUsername: String, newUsername: String){
    val lock = ReentrantLock()
    val condition = lock.newCondition()
    var matchRequests : MutableMap<Long, MutableList<String>>? = null

    GlobalScope.launch {
        FirebaseDbWrapper(context).readDbData(object :
            FirebaseDbWrapper.Companion.FirebaseReadCallback {
            override fun onDataChangeCallback(snapshot: DataSnapshot) {

                val children = snapshot.child("matchRequests").children
                for(child in children){
                    val childRequests = child.value as MutableList<String>
                    if(childRequests.contains(oldUsername)){
                        if(matchRequests == null){
                            matchRequests = mutableMapOf(child.key!!.toLong() to childRequests)
                        }
                        else{
                            matchRequests!![child.key!!.toLong()] = childRequests
                        }
                    }
                }


                lock.withLock {
                    condition.signal()
                }

            }

            override fun onCancelledCallback(error: DatabaseError) {
            }

        })
    }

    lock.withLock {
        condition.await()
    }

    if(matchRequests != null){
        for(matchId in matchRequests!!.keys){
            val request = matchRequests!![matchId]
            request!!.remove(oldUsername)
            request.add(newUsername)
            FirebaseDbWrapper(context).dbRef.child("matchRequests").child(matchId.toString()).setValue(request)
        }
    }
}

fun replaceUsernameInFriendRequests(context: Context, oldUsername: String, newUsername: String){
    val lock = ReentrantLock()
    val condition = lock.newCondition()
    var friendRequests : MutableMap<String, MutableList<String>>? = null

    GlobalScope.launch {
        FirebaseDbWrapper(context).readDbData(object :
            FirebaseDbWrapper.Companion.FirebaseReadCallback {
            override fun onDataChangeCallback(snapshot: DataSnapshot) {

                val children = snapshot.child("friendRequests").children
                for(child in children){
                    val childRequests = child.value as MutableList<String>
                    if(childRequests.contains(oldUsername)){
                        if(friendRequests == null){
                            friendRequests = mutableMapOf(child.key!! to childRequests)
                        }
                        else{
                            friendRequests!![child.key!!] = childRequests
                        }
                    }
                }


                lock.withLock {
                    condition.signal()
                }

            }

            override fun onCancelledCallback(error: DatabaseError) {
            }

        })
    }

    lock.withLock {
        condition.await()
    }

    if(friendRequests != null){
        for(uid in friendRequests!!.keys){
            val request = friendRequests!![uid]
            request!!.remove(oldUsername)
            request.add(newUsername)
            FirebaseDbWrapper(context).dbRef.child("friendRequests").child(uid).setValue(request)
        }
    }
}

fun replaceUsernameInChats(context: Context, oldUsername: String, newUsername: String){
    val lock = ReentrantLock()
    val condition = lock.newCondition()
    var chats : MutableList<Chat>? = null

    GlobalScope.launch {
        FirebaseDbWrapper(context).readDbData(object :
            FirebaseDbWrapper.Companion.FirebaseReadCallback {
            override fun onDataChangeCallback(snapshot: DataSnapshot) {

                val children = snapshot.child("chats").children
                for(child in children){
                    val chat = child.getValue(Chat::class.java)
                    if(chat!!.user1 == oldUsername || chat.user2 == oldUsername){
                        if(chats == null){
                            chats = mutableListOf(chat)
                        }
                        else{
                            chats!!.add(chat)
                        }
                    }
                }

                lock.withLock {
                    condition.signal()
                }

            }

            override fun onCancelledCallback(error: DatabaseError) {
            }

        })
    }

    lock.withLock {
        condition.await()
    }

    if(chats != null){
        for(chat in chats!!){
            if(chat.user1 == oldUsername){
                chat.user1 = newUsername
            }
            else{
                chat.user2 = newUsername
            }
            FirebaseDbWrapper(context).dbRef.child("chats").child(chat.id.toString()).setValue(chat)
        }
    }
}

class FirebaseDbWrapper(context: Context) {

    private val db =
        Firebase.database("https://easycalcio-aba73-default-rtdb.europe-west1.firebasedatabase.app/")

    val dbRef =
        db.reference

    private val uid = FirebaseAuthWrapper(context).getUid()

    fun writeNotCompletedUser() {
        dbRef.child("notCompletedUsers").child(uid!!).setValue(true)
    }

    fun writeUser(user: User) {
        dbRef.child("users").child(uid!!).setValue(user)
    }

    fun readDbData(callback: FirebaseReadCallback) {
        dbRef.addValueEventListener(FirebaseReadListener(callback))
    }

    companion object {

        class FirebaseReadListener(val callback: FirebaseReadCallback) : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                callback.onDataChangeCallback(snapshot)
            }

            override fun onCancelled(error: DatabaseError) {
                callback.onCancelledCallback(error)
            }
        }

        interface FirebaseReadCallback {
            fun onDataChangeCallback(snapshot: DataSnapshot);
            fun onCancelledCallback(error: DatabaseError);
        }
    }

}

class FirebaseStorageWrapper {
    private val storage = FirebaseStorage.getInstance()
    private val storageRef = storage.reference

    fun upload(image: Uri, name: String, context: Context) {
        storageRef.child("images/${name}.jpg").putFile(image).addOnSuccessListener {
            val intent = Intent(context, MainActivity::class.java)
            context.startActivity(intent)
        }
    }

    fun download(name: String): Uri? {
        val tmp = File.createTempFile(name, "jpg")
        var image: Uri? = null

        val lock = ReentrantLock()
        val condition = lock.newCondition()


        GlobalScope.launch {
            storageRef.child("images/${name}.jpg").getFile(tmp).addOnSuccessListener {
                image = Uri.fromFile(tmp)
                lock.withLock {
                    condition.signal()
                }
            }.addOnFailureListener {
                lock.withLock {
                    condition.signal()
                }
            }
        }
        lock.withLock {
            condition.await()
        }
        return image
    }

    fun delete(name: String) {
        storageRef.child("images/${name}.jpg").delete()
    }

}