package com.example.easycalcio.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Spinner
import com.example.easycalcio.R

class ProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val roleSpinner : Spinner = findViewById(R.id.profileRoleSpinner)
        val adapter : ArrayAdapter<CharSequence> = ArrayAdapter.createFromResource(this, R.array.roles, android.R.layout.simple_spinner_item)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        roleSpinner.adapter = adapter
        roleSpinner.isEnabled = false
        roleSpinner.isClickable = false

        //TODO: complete profile activity
    }
}