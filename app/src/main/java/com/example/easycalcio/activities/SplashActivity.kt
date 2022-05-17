package com.example.easycalcio.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.easycalcio.models.FirebaseWrapper

class SplashActivity:AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val tmp = Intent(this, ChatActivity::class.java)
        this.startActivity(tmp)
        finish()
        return

        // Check if user is logged or not
        val firebaseWrapper = FirebaseWrapper(this)
        if (!firebaseWrapper.isAuthenticated()) {
            // Redirect to login/register activity
            val intent = Intent(this, LoginActivity::class.java)
            this.startActivity(intent)
            finish()
            return
        }
        else{
            //TODO: check if user completed the registration
            val intent = Intent(this, MainActivity::class.java)
            this.startActivity(intent)
            finish()
        }
    }
}