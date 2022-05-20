package com.example.easycalcio.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.easycalcio.R

/**
 * A simple [Fragment] subclass.
 * Use the [UsersListFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class UsersListFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var query: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            query = it.getString("query")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        //TODO: retrieve the list of users with query
        //TODO: if list is empty display user not found fragment
        return inflater.inflate(R.layout.fragment_user_not_found, container, false)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment UsersListFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(queryString: String) =
            UsersListFragment().apply {
                arguments = Bundle().apply {
                    putString("query", queryString)
                }
            }
    }
}