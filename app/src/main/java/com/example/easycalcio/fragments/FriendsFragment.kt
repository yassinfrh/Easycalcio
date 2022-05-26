package com.example.easycalcio.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.example.easycalcio.R
import com.example.easycalcio.activities.SearchUserActivity
import com.example.easycalcio.models.FirebaseAuthWrapper
import com.google.android.material.floatingactionbutton.FloatingActionButton

class FriendsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_friends, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val fragmentManager = requireActivity().supportFragmentManager
        fragmentManager.commit {
            setReorderingAllowed(true)
            val frag = FriendsListFragment.newInstance(FirebaseAuthWrapper(view.context).getUid()!!)
            replace(R.id.friendsFragment, frag)
        }

        val searchButton: FloatingActionButton =
            requireView().findViewById(R.id.friendsSearchButton)
        searchButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View?) {
                val intent = Intent(view!!.context, SearchUserActivity::class.java)
                view.context.startActivity(intent)
            }

        })
    }

}