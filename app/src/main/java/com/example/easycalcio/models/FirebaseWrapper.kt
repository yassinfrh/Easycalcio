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
                            list!!.add(user)
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
    var requests : MutableList<String>? = null

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

    lock.withLock {
        condition.await()
    }

    if(requests == null){
        requests = mutableListOf(username)
    }
    else{
        requests!!.add(username)
    }

    FirebaseDbWrapper(context).db.child("friendRequests").child(uid).setValue(requests)
}

fun hasSentRequest(context: Context, username: String): Boolean {

    val uid = FirebaseAuthWrapper(context).getUid()!!
    val lock = ReentrantLock()
    val condition = lock.newCondition()
    var hasSent = false

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

    lock.withLock {
        condition.await()
    }
    return hasSent
}

fun getReceivedRequests(context: Context) : MutableList<String>?{
    val lock = ReentrantLock()
    var condition = lock.newCondition()
    var list: MutableList<String>? = null
    var loggedUser : User? = null

    GlobalScope.launch {
        loggedUser = getUser(context)

        lock.withLock {
            condition.signal()
        }
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
                    for(request in currRequests){
                        if(request == username){
                            if(list == null){
                                list = mutableListOf(child.key!!)
                            }
                            else{
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

    //translate uid to username
    if(list != null){
        condition = lock.newCondition()

        GlobalScope.launch {
            FirebaseDbWrapper(context).readDbData(object :
                FirebaseDbWrapper.Companion.FirebaseReadCallback {
                override fun onDataChangeCallback(snapshot: DataSnapshot) {
                    val children = snapshot.child("users").children
                    for (child in children) {
                        if (list!!.contains(child.key)){
                            val user = child.getValue(User::class.java)
                            list!![list!!.indexOf(child.key)] = user!!.username
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

    return list
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