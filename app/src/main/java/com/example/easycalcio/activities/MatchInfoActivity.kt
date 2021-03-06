package com.example.easycalcio.activities

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.commit
import com.example.easycalcio.R
import com.example.easycalcio.fragments.MatchPlayersFragment
import com.example.easycalcio.models.getMatch
import com.example.easycalcio.models.getUser
import com.example.easycalcio.models.quitMatch
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*

class MatchInfoActivity : AppCompatActivity() {

    var matchId: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_match_info)
        title = "Match info"

        matchId = intent.getLongExtra("matchId", 0)

        val organizerView: TextView = findViewById(R.id.matchOrganizer)
        val titleView: TextView = findViewById(R.id.matchTitle)
        val dateView: TextView = findViewById(R.id.matchDate)
        val timeView: TextView = findViewById(R.id.matchTime)
        val cityView: TextView = findViewById(R.id.matchCity)
        val addressView: TextView = findViewById(R.id.matchAddress)
        val playersNumberView: TextView = findViewById(R.id.matchPlayersNumber)

        val dateFormatter = SimpleDateFormat("yyyy/MM/dd", Locale.US)
        val timeFormatter = SimpleDateFormat("HH:mm", Locale.US)

        val fragmentManager = supportFragmentManager

        val dialogClickListener = object : DialogInterface.OnClickListener {
            override fun onClick(dialog: DialogInterface?, which: Int) {
                when (which) {
                    DialogInterface.BUTTON_POSITIVE -> {
                        CoroutineScope(Dispatchers.Main + Job()).launch {
                            withContext(Dispatchers.IO) {
                                quitMatch(this@MatchInfoActivity, matchId!!)
                                withContext(Dispatchers.Main) {
                                    val intent =
                                        Intent(this@MatchInfoActivity, MainActivity::class.java)
                                    this@MatchInfoActivity.startActivity(intent)
                                }
                            }
                        }
                    }
                }
            }
        }

        val quitButton: FloatingActionButton = findViewById(R.id.quitMatchButton)

        CoroutineScope(Dispatchers.Main + Job()).launch {
            withContext(Dispatchers.IO) {
                val user = getUser(this@MatchInfoActivity)
                withContext(Dispatchers.Main) {
                    if (user.matches!!.contains(matchId)) {
                        quitButton.visibility = View.VISIBLE
                        quitButton.setOnClickListener(object : View.OnClickListener {
                            override fun onClick(v: View?) {
                                val builder = AlertDialog.Builder(this@MatchInfoActivity)
                                builder.setMessage("Are you sure you wanna quit the match?")
                                    .setPositiveButton("Yes", dialogClickListener)
                                    .setNegativeButton("No", dialogClickListener).show()
                            }
                        })
                    }
                }
            }
        }

        CoroutineScope(Dispatchers.Main + Job()).launch {
            withContext(Dispatchers.IO) {
                val match = getMatch(this@MatchInfoActivity, matchId!!)
                withContext(Dispatchers.Main) {
                    organizerView.text = match.username
                    titleView.text = match.title
                    dateView.text = dateFormatter.format(match.date)
                    timeView.text = timeFormatter.format(match.date)
                    cityView.text = match.city
                    addressView.text = match.address
                    playersNumberView.text = match.playersNumber.toString()

                    fragmentManager.commit {
                        setReorderingAllowed(true)
                        val frag = MatchPlayersFragment.newInstance("*")
                        replace(R.id.matchPlayersFragment, frag)
                    }
                }
            }
        }
    }
}