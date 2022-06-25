package com.example.easycalcio.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.example.easycalcio.R
import com.example.easycalcio.models.FirebaseAuthWrapper

class MatchRequestsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_match_requests, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val fragmentManager = requireActivity().supportFragmentManager

        fragmentManager.commit {
            setReorderingAllowed(true)
            val frag = MatchRequestsListFragment.newInstance(FirebaseAuthWrapper(view.context).getUid()!!)
            replace(R.id.matchRequestsFragment, frag)
        }

    }
}