package com.example.easycalcio.activities

import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.app.TimePickerDialog
import android.app.TimePickerDialog.OnTimeSetListener
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.easycalcio.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.SimpleDateFormat
import java.util.*


class NewMatchActivity : AppCompatActivity() {

    private val myCalendar : Calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_match)
        title = "Create new match"

        val editTextDate : EditText = findViewById(R.id.newMatchDate)
        val editTextTime : EditText = findViewById(R.id.newMatchTime)

        val matchDate =
            OnDateSetListener { view, year, month, day ->
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
                datePicker.show()
            }
        })

        val matchTime = OnTimeSetListener { view, hour, minute ->
            myCalendar.set(Calendar.HOUR_OF_DAY, hour)
            myCalendar.set(Calendar.MINUTE, minute)
            val timeFormat = SimpleDateFormat("HH:mm", Locale.US)
            if(myCalendar.timeInMillis < System.currentTimeMillis()){
                Toast.makeText(view!!.context, "Select a future time!", Toast.LENGTH_SHORT).show()
            }
            else{
                editTextTime.setText(timeFormat.format(myCalendar.time))
            }
        }
        editTextTime.setOnClickListener(object : View.OnClickListener{
            override fun onClick(view: View?) {
                if(editTextDate.text.isNotEmpty()){
                    TimePickerDialog(view!!.context, matchTime, myCalendar[Calendar.HOUR_OF_DAY], myCalendar[Calendar.MINUTE], true).show()
                }
                else{
                    Toast.makeText(view!!.context, "Set the date first!", Toast.LENGTH_SHORT).show()
                }
            }
        })

        val playersNumberEditText : TextView = findViewById(R.id.newMatchPlayerNumber)
        val seekBar : SeekBar = findViewById(R.id.newMatchSeekBar)
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
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

        //TODO: friends list

        val saveButton : FloatingActionButton = findViewById(R.id.newMatchSaveButton)
        saveButton.setOnClickListener(object : View.OnClickListener{
            override fun onClick(view: View?) {
                val title : EditText = findViewById(R.id.newMatchTitle)
                val date : EditText = findViewById(R.id.newMatchDate)
                val time : EditText = findViewById(R.id.newMatchTime)
                val city : EditText = findViewById(R.id.newMatchCity)
                val address : EditText = findViewById(R.id.newMatchAddress)

                val views = arrayOf(title, date, time, city, address)

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

                //TODO: add to database and intent to MainActivity (do it using onCompleteListener in FirebaseWrapper)
            }

        })
    }
}