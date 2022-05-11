package com.example.easycalcio.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.easycalcio.R
import com.example.easycalcio.models.Match

/**
 * A simple [Fragment] subclass.
 * Use the [MatchListFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MatchListFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var userId: String? = null
    private var matches : List<Match>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            userId = it.getString("userId")
        }

        //TODO: retrieve the list of matches of the user from now on
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_match_list, container, false)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment MatchListFragment.
         */
        @JvmStatic
        fun newInstance(userId: String) =
            MatchListFragment().apply {
                arguments = Bundle().apply {
                    putString("userId", userId)
                }
            }
    }
}