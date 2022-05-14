package com.example.easycalcio.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.easycalcio.models.FirebaseWrapper

class SplashActivity:AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val tmp = Intent(this, RegistrationActivity::class.java)
        this.startActivity(tmp)
        finish()
        return

        // Check if user is logged or not
        val firebaseWrapper : FirebaseWrapper = FirebaseWrapper(this)
        if (!firebaseWrapper.isAuthenticated()) {
            // Redirect to login/register activity
            val intent = Intent(this, LoginActivity::class.java)
            this.startActivity(intent)
            finish()
            return
        }
        else{
            val intent = Intent(this, MainActivity::class.java)
            this.startActivity(intent)
            finish()
        }
    }
}