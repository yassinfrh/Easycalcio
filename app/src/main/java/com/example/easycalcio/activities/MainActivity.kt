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

        //TODO: display the matches

        //listener for the floatingActionButton
        val floatingActionButton : FloatingActionButton = findViewById(R.id.createMatchButton)
        floatingActionButton.setOnClickListener(object: View.OnClickListener{
            override fun onClick(view: View?) {
                val intent = Intent(view!!.context, NewMatchActivity::class.java)
                view.context.startActivity(intent)
                finish()
            }
        })
    }
}