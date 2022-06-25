package com.example.easycalcio.activities

import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.app.TimePickerDialog
import android.app.TimePickerDialog.OnTimeSetListener
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.easycalcio.R
import com.example.easycalcio.models.FirebaseDbWrapper
import com.example.easycalcio.models.Match
import com.example.easycalcio.models.createMatch
import com.example.easycalcio.models.getMatchId
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*


class NewMatchActivity : AppCompatActivity() {

    private val myCalendar: Calendar = Calendar.getInstance()
    var selectedFriends: MutableList<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_match)
        title = "Create new match"
        val username = intent.extras!!.getString("username")

        val editTextDate: EditText = findViewById(R.id.newMatchDate)
        val editTextTime: EditText = findViewById(R.id.newMatchTime)

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
                val datePicker = DatePickerDialog(
                    view!!.context,
                    matchDate,
                    myCalendar[Calendar.YEAR],
                    myCalendar[Calendar.MONTH],
                    myCalendar[Calendar.DAY_OF_MONTH]
                )
                datePicker.datePicker.minDate = System.currentTimeMillis() - 1000
                datePicker.show()
            }
        })

        val matchTime = OnTimeSetListener { view, hour, minute ->
            myCalendar.set(Calendar.HOUR_OF_DAY, hour)
            myCalendar.set(Calendar.MINUTE, minute)
            val timeFormat = SimpleDateFormat("HH:mm", Locale.US)
            if (myCalendar.timeInMillis < System.currentTimeMillis()) {
                Toast.makeText(view!!.context, "Select a future time!", Toast.LENGTH_SHORT).show()
            } else {
                editTextTime.setText(timeFormat.format(myCalendar.time))
            }
        }
        editTextTime.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View?) {
                if (editTextDate.text.isNotEmpty()) {
                    TimePickerDialog(
                        view!!.context,
                        matchTime,
                        myCalendar[Calendar.HOUR_OF_DAY],
                        myCalendar[Calendar.MINUTE],
                        true
                    ).show()
                } else {
                    Toast.makeText(view!!.context, "Set the date first!", Toast.LENGTH_SHORT).show()
                }
            }
        })

        val playersNumberEditText: TextView = findViewById(R.id.newMatchPlayerNumber)
        val seekBar: SeekBar = findViewById(R.id.newMatchSeekBar)
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
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

        val saveButton: FloatingActionButton = findViewById(R.id.newMatchSaveButton)
        saveButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View?) {
                val titleEdit: EditText = findViewById(R.id.newMatchTitle)
                val dateEdit: EditText = findViewById(R.id.newMatchDate)
                val timeEdit: EditText = findViewById(R.id.newMatchTime)
                val cityEdit: EditText = findViewById(R.id.newMatchCity)
                val addressEdit: EditText = findViewById(R.id.newMatchAddress)

                val views = arrayOf(titleEdit, dateEdit, timeEdit, cityEdit, addressEdit)

                var err = false
                for (v in views) {
                    v.error = null
                    if (v.text.isEmpty()) {
                        err = true
                        v.error = "Required field"
                    }
                }
                if (err)
                    return

                if (selectedFriends != null && seekBar.progress < selectedFriends!!.size) {
                    Toast.makeText(
                        this@NewMatchActivity,
                        "Select less friends!",
                        Toast.LENGTH_SHORT
                    ).show()
                    return
                }

                val formatter = SimpleDateFormat("yyyy/MM/dd HH:mm")

                GlobalScope.launch {
                    val matchId = getMatchId(this@NewMatchActivity)
                    val title = titleEdit.text.toString()
                    val date = formatter.parse(dateEdit.text.toString() + " " + timeEdit.text.toString())
                    val address = addressEdit.text.toString()
                    val city = cityEdit.text.toString()
                    val match = Match(
                        matchId,
                        title,
                        username!!,
                        date!!,
                        address,
                        city,
                        seekBar.progress,
                        mutableListOf(username)
                    )
                    createMatch(this@NewMatchActivity, match, selectedFriends)
                }
                val intent = Intent(this@NewMatchActivity, MainActivity::class.java)
                this@NewMatchActivity.startActivity(intent)
            }

        })
    }

}