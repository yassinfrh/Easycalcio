package com.example.easycalcio.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ListAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.commit
import com.example.easycalcio.R
import com.example.easycalcio.activities.EditMatchActivity
import com.example.easycalcio.activities.FriendProfileActivity
import com.example.easycalcio.activities.MainActivity
import com.example.easycalcio.activities.MatchInfoActivity
import com.example.easycalcio.models.*
import kotlinx.coroutines.*

class MatchListFragment : Fragment() {
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
        val view = inflater.inflate(R.layout.fragment_match_list, container, false)
        val matchesList: ListView = view.findViewById(R.id.matchesList)
        val fragmentManager = requireActivity().supportFragmentManager

        CoroutineScope(Dispatchers.Main + Job()).launch {
            withContext(Dispatchers.IO) {
                val matches = getMatches(view.context)
                val activity = requireActivity() as MainActivity
                withContext(Dispatchers.Main) {
                    if (matches != null) {
                        val adapter: ListAdapter =
                            MatchesArrayAdapter(requireActivity(), 0, matches)
                        matchesList.adapter = adapter
                        matchesList.onItemClickListener = object : AdapterView.OnItemClickListener {
                            override fun onItemClick(
                                parent: AdapterView<*>?,
                                view: View?,
                                position: Int,
                                id: Long
                            ) {
                                val matchId: Long =
                                    view!!.findViewById<TextView>(R.id.matchId).text.toString()
                                        .toLong()
                                val matchOrganizer: String =
                                    view.findViewById<TextView>(R.id.matchOrganizer).text.toString()

                                var intent: Intent? = null
                                intent = if (matchOrganizer == activity.user!!.username) {
                                    Intent(view.context, EditMatchActivity::class.java)
                                } else {
                                    Intent(view.context, MatchInfoActivity::class.java)
                                }

                                intent.putExtra("matchId", matchId)
                                view.context.startActivity(intent)
                            }

                        }
                    } else {
                        fragmentManager.commit {
                            setReorderingAllowed(true)
                            val frag = EmptyMatchListFragment()
                            replace(R.id.matchesFragment, frag)
                        }
                    }
                }
            }
        }

        return view
    }

    companion object {
        @JvmStatic
        fun newInstance(userId: String) =
            MatchListFragment().apply {
                arguments = Bundle().apply {
                    putString("userId", userId)
                }
            }
    }
}