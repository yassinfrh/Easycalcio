package com.example.easycalcio.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListAdapter
import android.widget.ListView
import com.example.easycalcio.R
import com.example.easycalcio.activities.MatchInfoActivity
import com.example.easycalcio.models.MatchPlayersArrayAdapter
import com.example.easycalcio.models.getMatch
import kotlinx.coroutines.*
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class MatchPlayersFragment : Fragment() {
    private var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            userId = it.getString("userId")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_match_players, container, false)
        val playersList: ListView = view.findViewById(R.id.matchPlayersList)
        val activity = requireActivity() as MatchInfoActivity

        CoroutineScope(Dispatchers.Main + Job()).launch {
            withContext(Dispatchers.IO) {

                val players = getMatch(view.context, activity.matchId!!).players!!
                withContext(Dispatchers.Main) {
                    val adapter : ListAdapter = MatchPlayersArrayAdapter(requireActivity(), 0, players)
                    playersList.adapter = adapter
                }
            }
        }
        return view
    }

    companion object {

        @JvmStatic
        fun newInstance(userId: String) =
            MatchPlayersFragment().apply {
                arguments = Bundle().apply {
                    putString("userId", userId)
                }
            }
    }
}