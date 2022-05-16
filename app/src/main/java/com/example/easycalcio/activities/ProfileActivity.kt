package com.example.easycalcio.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import com.example.easycalcio.R
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ProfileActivity : AppCompatActivity() {

    private var edit = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        title = "Profile"

        //TODO: set fields

        val profileChangePictureButton : Button = findViewById(R.id.profileChangePictureButton)
        val profileUsername : EditText = findViewById(R.id.profileUsername)
        val profileName : EditText = findViewById(R.id.profileName)
        val profileSurname : EditText = findViewById(R.id.profileSurname)
        val profileBirthday : EditText = findViewById(R.id.profileBirthday)
        val profileCity : EditText = findViewById(R.id.profileCity)

        val views = arrayOf(profileUsername, profileName, profileSurname, profileBirthday, profileCity)

        val roleSpinner : Spinner = findViewById(R.id.profileRoleSpinner)
        val adapter : ArrayAdapter<CharSequence> = ArrayAdapter.createFromResource(this, R.array.roles, android.R.layout.simple_spinner_item)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        roleSpinner.adapter = adapter
        roleSpinner.isEnabled = false
        roleSpinner.isClickable = false

        val profileButton : FloatingActionButton = findViewById(R.id.profileEditSaveButton)
        profileButton.setOnClickListener(object : View.OnClickListener{
            override fun onClick(view: View?) {
                edit = !edit
                if(edit){
                    profileButton.setImageResource(android.R.drawable.ic_menu_save)
                    profileChangePictureButton.visibility = Button.VISIBLE
                }
                else{
                    profileButton.setImageResource(android.R.drawable.ic_menu_edit)
                    profileChangePictureButton.visibility = Button.GONE
                }
                for(v in views){
                    v.isClickable = edit
                    v.isCursorVisible = edit
                    v.isFocusable = edit
                    v.isFocusableInTouchMode = edit
                }
                roleSpinner.isEnabled = edit
                roleSpinner.isClickable = edit

                //TODO: save to database
            }

        })
    }
}