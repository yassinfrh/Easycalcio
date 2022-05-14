package com.example.easycalcio.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.easycalcio.R
import androidx.fragment.app.commit
import com.example.easycalcio.fragments.LogInFragment
import com.example.easycalcio.fragments.SignUpFragment

class LoginActivity : AppCompatActivity() {
    var isLogin : Boolean = false
    var fragmentManager : FragmentManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Render fragment
        this.fragmentManager = this.getSupportFragmentManager()
        renderFragment();
    }

    private fun renderFragment() {
        val frag : Fragment
        if (isLogin) {
            // Login
            frag = LogInFragment()
        } else {
            // Creation
            frag = SignUpFragment()
        }

        fragmentManager!!.commit {
            setReorderingAllowed(true)
            this.replace(R.id.loginRegisterFragment, frag)
        }
    }

    fun switchFragment() {
        isLogin = !isLogin
        renderFragment()
    }
}