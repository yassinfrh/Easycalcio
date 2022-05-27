package com.example.easycalcio.models

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
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
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
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

fun registerUser(context: Context){
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
                            for(element in list!!){
                                if(element.username == user.username){
                                    contains = true
                                    break
                                }
                            }
                            if(!contains){
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

    FirebaseDbWrapper(context).db.child("friendRequests").child(uid).setValue(requests)
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
                    val currRequests = child.getValue() as List<String>
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
    val db = FirebaseDbWrapper(context).db
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
    val db = FirebaseDbWrapper(context).db
    db.child("friendRequests").child(senderUid!!).setValue(requestsList)
}

fun removeFriend(context: Context, friendUsername: String){

    val db = FirebaseDbWrapper(context).db
    val loggedUserUid = FirebaseAuthWrapper(context).getUid()!!
    val lock1 = ReentrantLock()
    val condition1 = lock1.newCondition()

    var loggedUser : User? = null

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
    var friendUser : User? = null
    var friendUserUid : String? = null
    GlobalScope.launch {
        FirebaseDbWrapper(context).readDbData(object :
            FirebaseDbWrapper.Companion.FirebaseReadCallback {
            override fun onDataChangeCallback(snapshot: DataSnapshot) {
                val children = snapshot.child("users").children
                for(child in children){
                    val curr = child.getValue(User::class.java)
                    if(curr!!.username == friendUsername){
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

fun getFriendsList(context: Context) : MutableList<User>?{

    val lock1 = ReentrantLock()
    val condition1 = lock1.newCondition()

    var friendsUsernames : MutableList<String>? = null
    GlobalScope.launch {
        friendsUsernames = getUser(context).friends
        lock1.withLock {
            condition1.signal()
        }
    }
    lock1.withLock {
        condition1.await()
    }
    if(friendsUsernames == null){
        return null
    }

    val lock2 = ReentrantLock()
    val condition2 = lock2.newCondition()

    var friends : MutableList<User>? = null
    GlobalScope.launch {
        FirebaseDbWrapper(context).readDbData(object :
            FirebaseDbWrapper.Companion.FirebaseReadCallback {
            override fun onDataChangeCallback(snapshot: DataSnapshot) {
                val children = snapshot.child("users").children
                for(username in friendsUsernames!!){
                    for(child in children){
                        val user = child.getValue(User::class.java)
                        if(username == user!!.username){
                            if(friends == null){
                                friends = mutableListOf(user)
                            }
                            else{
                                friends!!.add(user)
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

class FirebaseDbWrapper(private val context: Context) {

    val db =
        Firebase.database("https://easycalcio-aba73-default-rtdb.europe-west1.firebasedatabase.app/").reference

    private val uid = FirebaseAuthWrapper(context).getUid()

    fun writeNotCompletedUser() {
        db.child("notCompletedUsers").child(uid!!).setValue(true)
    }

    fun writeUser(user: User) {
        db.child("users").child(uid!!).setValue(user)
    }

    fun readDbData(callback: FirebaseReadCallback) {
        db.addValueEventListener(FirebaseReadListener(callback))
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