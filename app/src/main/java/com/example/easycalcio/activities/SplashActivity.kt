package com.example.easycalcio.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.easycalcio.R
import com.example.easycalcio.models.FirebaseAuthWrapper
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        // Check if user is logged or not
        val firebaseAuthWrapper = FirebaseAuthWrapper(this)
        if (!firebaseAuthWrapper.isAuthenticated()) {
            // Redirect to login/register activity
            val intent = Intent(this, LoginActivity::class.java)
            this.startActivity(intent)
            finish()
            return
        } else {
            val thiz = this
            GlobalScope.launch {
                val completed = FirebaseAuthWrapper(thiz).isCompleted()
                if (completed) {
                    val intent = Intent(thiz, MainActivity::class.java)
                    thiz.startActivity(intent)
                    finish()
                } else {
                    val intent = Intent(thiz, RegistrationActivity::class.java)
                    thiz.startActivity(intent)
                    finish()
                }
            }
        }
    }
}