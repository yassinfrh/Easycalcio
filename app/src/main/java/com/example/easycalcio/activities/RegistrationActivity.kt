package com.example.easycalcio.activities

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.easycalcio.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.SimpleDateFormat
import java.util.*

class RegistrationActivity : AppCompatActivity() {

    private val myCalendar : Calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)
        title = "Complete your registration"

        val registrationBirthday : EditText = findViewById(R.id.registrationBirthday)
        val date =
            DatePickerDialog.OnDateSetListener { view, year, month, day ->
                myCalendar.set(Calendar.YEAR, year)
                myCalendar.set(Calendar.MONTH, month)
                myCalendar.set(Calendar.DAY_OF_MONTH, day)
                val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.US)
                registrationBirthday.setText(dateFormat.format(myCalendar.time))
            }
        registrationBirthday.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View?) {
                val datePicker = DatePickerDialog(view!!.context, date, myCalendar[Calendar.YEAR], myCalendar[Calendar.MONTH], myCalendar[Calendar.DAY_OF_MONTH])
                datePicker.datePicker.maxDate = System.currentTimeMillis() - 315569260000 //10 years
                datePicker.show()
            }
        })

        val roleSpinner : Spinner = findViewById(R.id.registrationRoleSpinner)
        val adapter : ArrayAdapter<CharSequence> = ArrayAdapter.createFromResource(this, R.array.roles, android.R.layout.simple_spinner_item)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        roleSpinner.adapter = adapter

        val nextButton : FloatingActionButton = findViewById(R.id.nextButton)
        nextButton.setOnClickListener(object: View.OnClickListener{
            override fun onClick(view: View?) {
                val username : EditText = findViewById(R.id.registrationUsername)
                val name : EditText = findViewById(R.id.registrationName)
                val surname : EditText = findViewById(R.id.registrationSurname)
                val birthday : EditText = findViewById(R.id.registrationBirthday)
                val city : EditText = findViewById(R.id.registrationCity)
                val views = arrayOf(username, name, surname, birthday, city)
                val role : Spinner = findViewById(R.id.registrationRoleSpinner)

                var err = false;
                for (v in views){
                    v.error = null
                    if(v.text.isEmpty()){
                        err = true
                        v.error = "Required field"
                    }
                }
                if((role.selectedItem as String) == "Select a role.."){
                    err = true
                    Toast.makeText(view!!.context, "Select a role!", Toast.LENGTH_SHORT).show()
                }
                if(err)
                    return

                //TODO: add to database and intent to MainActivity (do it using onCompleteListener in FirebaseWrapper)


            }
        })
    }
}