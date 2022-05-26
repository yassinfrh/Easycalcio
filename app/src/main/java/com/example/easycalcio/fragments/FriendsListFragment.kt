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
import com.example.easycalcio.activities.FriendProfileActivity
import com.example.easycalcio.models.Match
import com.example.easycalcio.models.User
import com.example.easycalcio.models.UsersArrayAdapter
import com.example.easycalcio.models.getFriendsList
import kotlinx.coroutines.*

class FriendsListFragment : Fragment() {
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
        val view = inflater.inflate(R.layout.fragment_friends_list, container, false)
        val friendsList : ListView = view.findViewById(R.id.friendsList)
        val fragmentManager = requireActivity().supportFragmentManager
        CoroutineScope(Dispatchers.Main + Job()).launch {
            withContext(Dispatchers.IO) {
                val friends = getFriendsList(view.context)
                withContext(Dispatchers.Main) {
                    if(friends != null){
                        val adapter : ListAdapter = UsersArrayAdapter(requireActivity(), 0, friends)
                        friendsList.adapter = adapter
                        friendsList.onItemClickListener =
                            object : AdapterView.OnItemClickListener {
                                override fun onItemClick(
                                    parent: AdapterView<*>?,
                                    view: View?,
                                    position: Int,
                                    id: Long
                                ) {
                                    val username : String = view!!.findViewById<TextView>(R.id.friendUsername).text.toString()
                                    val intent = Intent(view.context, FriendProfileActivity::class.java)
                                    intent.putExtra("username", username)
                                    view.context.startActivity(intent)
                                }

                            }
                    }
                    else{
                        fragmentManager.commit {
                            setReorderingAllowed(true)
                            val frag = SearchUserFragment()
                            replace(R.id.friendsFragment, frag)
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
            FriendsListFragment().apply {
                arguments = Bundle().apply {
                    putString("userId", userId)
                }
            }
    }
}