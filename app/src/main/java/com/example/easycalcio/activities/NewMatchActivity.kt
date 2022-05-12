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
import androidx.appcompat.app.AppCompatActivity
import com.example.easycalcio.R
import java.text.SimpleDateFormat
import java.util.*
import kotlin.time.Duration.Companion.milliseconds


class NewMatchActivity : AppCompatActivity() {

    private val myCalendar : Calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_match)

        val editTextDate : EditText = findViewById(R.id.editTextDate)
        val editTextTime : EditText = findViewById(R.id.editTextTime)

        val date =
            OnDateSetListener { view, year, month, day ->
                myCalendar.set(Calendar.YEAR, year)
                myCalendar.set(Calendar.MONTH, month)
                myCalendar.set(Calendar.DAY_OF_MONTH, day)
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                editTextDate.setText(dateFormat.format(myCalendar.time))
                editTextTime.setText("")
            }
        editTextDate.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View?) {
                val datePicker = DatePickerDialog(view!!.context, date, myCalendar[Calendar.YEAR], myCalendar[Calendar.MONTH], myCalendar[Calendar.DAY_OF_MONTH])
                datePicker.datePicker.minDate = System.currentTimeMillis() - 1000
                datePicker.show()
            }
        })

        val time = OnTimeSetListener { view, hour, minute ->
            myCalendar.set(Calendar.HOUR_OF_DAY, hour)
            myCalendar.set(Calendar.MINUTE, minute)
            val timeFormat = SimpleDateFormat("HH:mm", Locale.US)
            editTextTime.setText(timeFormat.format(myCalendar.time))
        }
        editTextTime.setOnClickListener(object : View.OnClickListener{
            override fun onClick(view: View?) {
                if(editTextDate.text.isNotEmpty()){
                    val timePicker = TimePickerDialog(view!!.context, time, myCalendar[Calendar.HOUR_OF_DAY], myCalendar[Calendar.MINUTE], true)
                    //TODO: alert user if they're clicking on time without setting date first
                    //TODO: set minTime in timePicker
                }
            }
        })

        val playersNumberEditText : TextView = findViewById(R.id.playersNumberTextView)
        val seekBar : SeekBar = findViewById(R.id.seekBar)
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
    }
}