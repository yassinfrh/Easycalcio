package com.example.easycalcio.activities

import android.app.DatePickerDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import com.example.easycalcio.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.SimpleDateFormat
import java.util.*

class ProfileActivity : AppCompatActivity() {

    private var edit = false
    private val myCalendar : Calendar = Calendar.getInstance()


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

        val date =
            DatePickerDialog.OnDateSetListener { view, year, month, day ->
                myCalendar.set(Calendar.YEAR, year)
                myCalendar.set(Calendar.MONTH, month)
                myCalendar.set(Calendar.DAY_OF_MONTH, day)
                val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.US)
                profileBirthday.setText(dateFormat.format(myCalendar.time))
            }
        profileBirthday.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View?) {
                val datePicker = DatePickerDialog(view!!.context, date, myCalendar[Calendar.YEAR], myCalendar[Calendar.MONTH], myCalendar[Calendar.DAY_OF_MONTH])
                datePicker.datePicker.maxDate = System.currentTimeMillis() - 315569260000 //10 years
                if(edit){
                    datePicker.show()
                }
            }
        })

        //TODO: edit profile picture

        val profileButton : FloatingActionButton = findViewById(R.id.profileEditSaveButton)
        profileButton.setOnClickListener(object : View.OnClickListener{
            override fun onClick(view: View?) {

                edit = !edit

                if(edit){
                    profileButton.setImageResource(android.R.drawable.ic_menu_save)
                    profileChangePictureButton.visibility = Button.VISIBLE
                    for(v in views){
                        if(v != profileBirthday){
                            v.isClickable = true
                            v.isCursorVisible = true
                            v.isFocusable = true
                            v.isFocusableInTouchMode = true
                        }
                    }
                    roleSpinner.isEnabled = true
                    roleSpinner.isClickable = true
                }
                else{
                    var err = false;
                    for (v in views){
                        v.error = null
                        if(v.text.isEmpty()){
                            err = true
                            v.error = "Required field"
                        }
                    }
                    if((roleSpinner.selectedItem as String) == "Select a role.."){
                        err = true
                        Toast.makeText(view!!.context, "Select a role!", Toast.LENGTH_SHORT).show()
                    }
                    if(err)
                        return

                    for(v in views){
                        if(v != profileBirthday){
                            v.isClickable = false
                            v.isCursorVisible = false
                            v.isFocusable = false
                            v.isFocusableInTouchMode = false
                        }
                    }
                    roleSpinner.isEnabled = false
                    roleSpinner.isClickable = false

                    profileButton.setImageResource(android.R.drawable.ic_menu_edit)
                    profileChangePictureButton.visibility = Button.GONE
                    //TODO: save to database
                }

            }

        })
    }
}