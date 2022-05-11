package com.example.easycalcio.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.example.easycalcio.R
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val floatingActionButton : FloatingActionButton = findViewById(R.id.floatingActionButton)
        floatingActionButton.setOnClickListener(object: View.OnClickListener{
            override fun onClick(p0: View?) {
                //TODO: create intent to NewMatchActivity
                val intent = Intent(this, NewMatchActivity::class.java)
            }
        })
    }
}