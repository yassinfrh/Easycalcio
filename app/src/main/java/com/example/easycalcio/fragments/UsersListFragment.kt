package com.example.easycalcio.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.fragment.app.commit
import com.example.easycalcio.R
import com.example.easycalcio.models.UsersArrayAdapter
import com.example.easycalcio.models.getUsersStartingWith
import kotlinx.coroutines.*

class UsersListFragment : Fragment() {
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
        val view = inflater.inflate(R.layout.fragment_users_list, container, false)
        val resultList: ListView = view.findViewById(R.id.resultList)
        val fragmentManager = requireActivity().supportFragmentManager

        CoroutineScope(Dispatchers.Main + Job()).launch {
            withContext(Dispatchers.IO) {
                if (query != null && query!!.isNotEmpty()) {
                    val users =
                        getUsersStartingWith(this@UsersListFragment.requireContext(), query!!)
                    withContext(Dispatchers.Main) {
                        if (users != null) {
                            val adapter = UsersArrayAdapter(requireActivity(), 0, users)
                            resultList.adapter = adapter
                        } else {
                            fragmentManager.commit {
                                setReorderingAllowed(true)
                                val frag = UserNotFoundFragment()
                                replace(R.id.usersSearchFragment, frag)
                            }
                        }
                    }
                }
            }
        }


        return view
    }

    companion object {
        @JvmStatic
        fun newInstance(queryString: String) =
            UsersListFragment().apply {
                arguments = Bundle().apply {
                    putString("query", queryString)
                }
            }
    }
}