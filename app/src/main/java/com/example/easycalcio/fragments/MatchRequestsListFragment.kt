package com.example.easycalcio.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListAdapter
import android.widget.ListView
import androidx.fragment.app.commit
import com.example.easycalcio.R
import com.example.easycalcio.models.*
import kotlinx.coroutines.*

class MatchRequestsListFragment : Fragment() {
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
        val view = inflater.inflate(R.layout.fragment_match_requests_list, container, false)
        val requestsList : ListView = view.findViewById(R.id.matchRequestsList)
        val fragmentManager = requireActivity().supportFragmentManager

        CoroutineScope(Dispatchers.Main + Job()).launch {
            withContext(Dispatchers.IO) {
                val matches = getMatchRequests(view.context)
                withContext(Dispatchers.Main) {
                    if(matches != null){
                        val adapter : ListAdapter = MatchRequestsArrayAdapter(requireActivity(), 0, matches)
                        requestsList.adapter = adapter
                    }
                    else{
                        fragmentManager.commit {
                            setReorderingAllowed(true)
                            val frag = EmptyMatchesRequestsFragment()
                            replace(R.id.matchRequestsFragment, frag)
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
            MatchRequestsListFragment().apply {
                arguments = Bundle().apply {
                    putString("userId", userId)
                }
            }
    }
}