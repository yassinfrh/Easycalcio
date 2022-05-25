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
import com.example.easycalcio.models.FriendRequestsArrayAdapter
import com.example.easycalcio.models.getReceivedRequests
import kotlinx.coroutines.*

class FriendRequestsListFragment : Fragment() {
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
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_friend_requests_list, container, false)
        val requestsList : ListView = view.findViewById(R.id.requestsList)
        val fragmentManager = requireActivity().supportFragmentManager

        CoroutineScope(Dispatchers.Main + Job()).launch {
            withContext(Dispatchers.IO) {
                val users = getReceivedRequests(view.context)
                withContext(Dispatchers.Main) {
                    if(users != null){
                        val adapter : ListAdapter = FriendRequestsArrayAdapter(requireActivity(), 0, users)
                        requestsList.adapter = adapter
                    }
                    else{
                        fragmentManager.commit {
                            setReorderingAllowed(true)
                            val frag = EmptyFriendRequestsFragment()
                            replace(R.id.friendRequestsFragment, frag)
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
            FriendRequestsListFragment().apply {
                arguments = Bundle().apply {
                    putString("userId", userId)
                }
            }
    }
}