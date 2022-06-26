package com.example.easycalcio.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.fragment.app.commit
import com.example.easycalcio.R
import com.example.easycalcio.fragments.MatchPlayersFragment
import com.example.easycalcio.models.getMatch
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*

class MatchInfoActivity : AppCompatActivity() {

    var matchId : Long? = null

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