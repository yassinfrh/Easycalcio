package com.example.easycalcio.models

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.example.easycalcio.activities.RegistrationActivity
import com.example.easycalcio.activities.SplashActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class FirebaseWrapper(private val context: Context) {
    private val TAG : String = FirebaseWrapper::class.simpleName.toString()
    private var auth: FirebaseAuth = Firebase.auth
    private var database = Firebase.database("https://easycalcio-aba73-default-rtdb.europe-west1.firebasedatabase.app/").reference

    fun isAuthenticated() : Boolean {
        return auth.currentUser != null
    }

    //TODO: check if user has completed registration

    fun signUp(email: String, password: String) {
        this.auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d(TAG, "createUserWithEmail:success")

                //add not completely registered to database
                val newUser = RegisteredUser(email, false)
                database.child("completedUsers").push().setValue(newUser)

                val intent = Intent(this.context, RegistrationActivity::class.java)
                context.startActivity(intent)
            } else {
                // If sign in fails, display a message to the user.
                Log.w(TAG, "createUserWithEmail:failure", task.exception)
                Toast.makeText(context, "Sign-up failed. Error message: ${task.exception!!.message}", Toast.LENGTH_LONG).show()
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

}

class RegisteredUser(val email: String, val completed : Boolean)