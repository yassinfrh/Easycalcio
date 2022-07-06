package com.example.easycalcio.activities

import android.app.AlertDialog
import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import com.example.easycalcio.R
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.easycalcio.fragments.*
import com.example.easycalcio.models.*
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity() {

    private var drawer: DrawerLayout? = null
    var user: User? = null
    var headerUsername: TextView? = null
    var headerProfileImage: ImageView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        runInstantWorker(this)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        drawer = findViewById(R.id.drawer_layout)

        val navigationView: NavigationView = findViewById(R.id.nav_view)

        val header = navigationView.getHeaderView(0)
        headerUsername = header.findViewById(R.id.nav_username)
        headerProfileImage = header.findViewById(R.id.profileImage)

        CoroutineScope(Dispatchers.Main + Job()).launch {
            withContext(Dispatchers.IO) {
                user = getUser(this@MainActivity)
                val image = FirebaseStorageWrapper().download(user!!.username.lowercase())
                withContext(Dispatchers.Main) {
                    headerUsername!!.text = user!!.username
                    if (image != null) {
                        headerProfileImage!!.setImageURI(image)
                    }
                }
            }
        }

        val dialogClickListener = object : DialogInterface.OnClickListener {
            override fun onClick(dialog: DialogInterface?, which: Int) {
                when (which) {
                    DialogInterface.BUTTON_POSITIVE -> {
                        FirebaseAuthWrapper(this@MainActivity).signOut()
                        finish()
                    }
                }
            }
        }

        navigationView.setNavigationItemSelectedListener(object :
            NavigationView.OnNavigationItemSelectedListener {
            override fun onNavigationItemSelected(item: MenuItem): Boolean {

                when (item.itemId) {
                    R.id.nav_home -> supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, HomeFragment()).commit()
                    R.id.nav_profile -> supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, ProfileFragment()).commit()
                    R.id.nav_friends -> supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, FriendsFragment()).commit()
                    R.id.nav_friendRequests -> supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, FriendRequestsFragment()).commit()
                    R.id.nav_matchRequests -> supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, MatchRequestsFragment()).commit()
                    R.id.nav_logout -> {
                        val builder = AlertDialog.Builder(this@MainActivity)
                        builder.setMessage("Are you sure you wanna logout?")
                            .setPositiveButton("Yes", dialogClickListener)
                            .setNegativeButton("No", dialogClickListener).show()
                    }
                }
                drawer!!.closeDrawer(GravityCompat.START)
                return true
            }

        })

        val toggle = ActionBarDrawerToggle(
            this,
            drawer,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawer!!.addDrawerListener(toggle)
        toggle.syncState()

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HomeFragment()).commit()
            navigationView.setCheckedItem(R.id.nav_home)
        }

    }

    override fun onBackPressed() {
        if (drawer!!.isDrawerOpen(GravityCompat.START)) {
            drawer!!.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }



}