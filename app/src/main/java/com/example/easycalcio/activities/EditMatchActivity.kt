package com.example.easycalcio.activities

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import com.example.easycalcio.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.SimpleDateFormat
import java.util.*

class EditMatchActivity : AppCompatActivity() {

    private val myCalendar : Calendar = Calendar.getInstance()
    var edit = false
    var selectedFriends: MutableList<String>? = null
    var matchId : Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_match)
        title = "Edit match"
        matchId = intent.getLongExtra("matchId", 0)

        //TODO: set fields

        val editTitle : EditText = findViewById(R.id.editMatchTitle)
        val editTextDate : EditText = findViewById(R.id.editMatchDate)
        val editTextTime : EditText = findViewById(R.id.editMatchTime)
        val editCity : EditText = findViewById(R.id.editMatchCity)
        val editAddress : EditText = findViewById(R.id.editMatchAddress)

        val views = arrayOf(editTitle, editTextDate, editTextTime, editCity, editAddress)

        val editSeekBar : SeekBar = findViewById(R.id.editMatchSeekBar)
        val saveButton : FloatingActionButton = findViewById(R.id.editMatchSaveButton)

        editSeekBar.isEnabled = false

        val matchDate =
            DatePickerDialog.OnDateSetListener { view, year, month, day ->
                myCalendar.set(Calendar.YEAR, year)
                myCalendar.set(Calendar.MONTH, month)
                myCalendar.set(Calendar.DAY_OF_MONTH, day)
                val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.US)
                editTextDate.setText(dateFormat.format(myCalendar.time))
                editTextTime.setText("")
            }
        editTextDate.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View?) {
                val datePicker = DatePickerDialog(view!!.context, matchDate, myCalendar[Calendar.YEAR], myCalendar[Calendar.MONTH], myCalendar[Calendar.DAY_OF_MONTH])
                datePicker.datePicker.minDate = System.currentTimeMillis() - 1000
                if(edit){
                    datePicker.show()
                }
            }
        })

        val matchTime = TimePickerDialog.OnTimeSetListener { view, hour, minute ->
            myCalendar.set(Calendar.HOUR_OF_DAY, hour)
            myCalendar.set(Calendar.MINUTE, minute)
            val timeFormat = SimpleDateFormat("HH:mm", Locale.US)
            if (myCalendar.timeInMillis < System.currentTimeMillis()) {
                Toast.makeText(view!!.context, "Select a future time!", Toast.LENGTH_SHORT).show()
            } else {
                editTextTime.setText(timeFormat.format(myCalendar.time))
            }
        }
        editTextTime.setOnClickListener(object : View.OnClickListener{
            override fun onClick(view: View?) {
                if(edit){
                    if(editTextDate.text.isNotEmpty()){
                        TimePickerDialog(view!!.context, matchTime, myCalendar[Calendar.HOUR_OF_DAY], myCalendar[Calendar.MINUTE], true).show()
                    }
                    else{
                        Toast.makeText(view!!.context, "Set the date first!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })

        val playersNumberEditText : TextView = findViewById(R.id.editMatchPlayerNumber)

        editSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(bar: SeekBar?, progress: Int, fromUser: Boolean) {
                val value = progress - (progress % 2)
                bar!!.progress = value
                playersNumberEditText.text = value.toString()
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
            }
        })

        //TODO: delete match button

        saveButton.setOnClickListener(object : View.OnClickListener{
            override fun onClick(view: View?) {

                edit = !edit


                if(edit){
                    saveButton.setImageResource(android.R.drawable.ic_menu_save)
                    for(v in views){
                        if(v != editTextDate && v != editTextTime){
                            v.isClickable = true
                            v.isCursorVisible = true
                            v.isFocusable = true
                            v.isFocusableInTouchMode = true
                        }
                    }
                    editSeekBar.isClickable = true
                    editSeekBar.isFocusable = true
                    editSeekBar.isFocusableInTouchMode = true
                    editSeekBar.isEnabled = true
                }
                else{
                    var err = false
                    for(v in views){
                        v.error = null
                        if(v.text.isEmpty()){
                            err = true
                            v.error = "Required field"
                        }
                    }
                    if(err)
                        return

                    for(v in views){
                        if(v != editTextDate && v!= editTextTime){
                            v.isClickable = false
                            v.isCursorVisible = false
                            v.isFocusable = false
                            v.isFocusableInTouchMode = false
                        }
                    }
                    editSeekBar.isClickable = false
                    editSeekBar.isFocusable = false
                    editSeekBar.isFocusableInTouchMode = false
                    editSeekBar.isEnabled = false

                    //TODO: add to database and intent to MainActivity (do it using onCompleteListener in FirebaseWrapper)

                    saveButton.setImageResource(android.R.drawable.ic_menu_edit)
                }

            }

        })
    }
}