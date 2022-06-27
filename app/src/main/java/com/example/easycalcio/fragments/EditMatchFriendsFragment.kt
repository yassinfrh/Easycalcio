package com.example.easycalcio.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ListAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.example.easycalcio.R
import com.example.easycalcio.activities.EditMatchActivity
import com.example.easycalcio.models.UsersArrayAdapter
import com.example.easycalcio.models.getFriendsList
import com.example.easycalcio.models.getMatch
import kotlinx.coroutines.*

class EditMatchFriendsFragment : Fragment()  {

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
        val view = inflater.inflate(R.layout.fragment_match_friends, container, false)
        val friendsList : ListView = view.findViewById(R.id.matchFriendsList)


        val fragmentManager = requireActivity().supportFragmentManager
        val activity = requireActivity() as EditMatchActivity

        CoroutineScope(Dispatchers.Main + Job()).launch {
            withContext(Dispatchers.IO) {
                val friends = getFriendsList(view.context)
                withContext(Dispatchers.Main) {
                    if (friends != null) {
                        val adapter: ListAdapter = UsersArrayAdapter(requireActivity(), 0, friends)
                        friendsList.adapter = adapter
                        friendsList.onItemClickListener =
                            object : AdapterView.OnItemClickListener {
                                override fun onItemClick(
                                    parent: AdapterView<*>?,
                                    view: View?,
                                    position: Int,
                                    id: Long
                                ) {
                                    if(activity.edit){
                                        val username: String =
                                            view!!.findViewById<TextView>(R.id.friendUsername).text.toString()
                                        if(activity.selectedFriends == null){
                                            activity.selectedFriends = mutableListOf(username)
                                            view.setBackgroundColor(Color.parseColor("#79f29d"))
                                        }
                                        else{
                                            if(activity.selectedFriends!!.contains(username)){
                                                activity.selectedFriends!!.remove(username)
                                                view.setBackgroundColor(ContextCompat.getColor(activity, androidx.cardview.R.color.cardview_shadow_end_color))
                                            }
                                            else{
                                                activity.selectedFriends!!.add(username)
                                                view.setBackgroundColor(Color.parseColor("#79f29d"))
                                            }
                                        }
                                    }
                                }

                            }
                    } else {
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
            MatchFriendsFragment().apply {
                arguments = Bundle().apply {
                    putString("userId", userId)
                }
            }
    }

}