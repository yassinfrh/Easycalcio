package com.example.easycalcio.activities

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.easycalcio.R
import com.example.easycalcio.models.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*

class RegistrationActivity : AppCompatActivity() {

    private val myCalendar: Calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)
        title = "Complete your registration"

        val registrationBirthday: EditText = findViewById(R.id.registrationBirthday)
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
                val datePicker = DatePickerDialog(
                    view!!.context,
                    date,
                    myCalendar[Calendar.YEAR],
                    myCalendar[Calendar.MONTH],
                    myCalendar[Calendar.DAY_OF_MONTH]
                )
                datePicker.datePicker.maxDate = System.currentTimeMillis() - 315569260000 //10 years
                datePicker.show()
            }
        })

        val roleSpinner: Spinner = findViewById(R.id.registrationRoleSpinner)
        val adapter: ArrayAdapter<CharSequence> = ArrayAdapter.createFromResource(
            this,
            R.array.roles,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        roleSpinner.adapter = adapter

        var usernameErr = false

        val usernameEditText: EditText = findViewById(R.id.registrationUsername)
        usernameEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                usernameErr = false
                CoroutineScope(Dispatchers.Main + Job()).launch {
                    withContext(Dispatchers.IO) {
                        val used = alreadyUsedUsername(
                            this@RegistrationActivity,
                            usernameEditText.text.toString()
                        )
                        withContext(Dispatchers.Main) {
                            if (used) {
                                usernameErr = true
                                usernameEditText.error = "Already used username"
                            }
                        }
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {
            }

        })

        var err: Boolean
        val nextButton: FloatingActionButton = findViewById(R.id.nextButton)
        nextButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View?) {
                val username: EditText = findViewById(R.id.registrationUsername)
                val name: EditText = findViewById(R.id.registrationName)
                val surname: EditText = findViewById(R.id.registrationSurname)
                val birthday: EditText = findViewById(R.id.registrationBirthday)
                val city: EditText = findViewById(R.id.registrationCity)
                val views = arrayOf(username, name, surname, birthday, city)
                val role: Spinner = findViewById(R.id.registrationRoleSpinner)

                err = false
                for (v in views) {
                    v.error = null
                    if (v.text.isEmpty()) {
                        err = true
                        v.error = "Required field"
                    }
                }
                if ((role.selectedItem as String) == "Select a role..") {
                    err = true
                    Toast.makeText(view!!.context, "Select a role!", Toast.LENGTH_SHORT).show()
                }
                if (err || usernameErr)
                    return


                val formatter = SimpleDateFormat("yyyy/MM/dd")

                val user = User(
                    username.text.toString().lowercase(),
                    name.text.toString(),
                    surname.text.toString(),
                    formatter.parse(birthday.text.toString()),
                    city.text.toString(),
                    role.selectedItem.toString(),
                    mutableListOf(),
                    mutableListOf()
                )
                //write user in db
                FirebaseDbWrapper(view!!.context).writeUser(user)
                GlobalScope.launch {
                    registerUser(view.context)
                }
                val intent = Intent(view.context, SplashActivity::class.java)
                view.context.startActivity(intent)
                finish()
            }
        })
    }
}