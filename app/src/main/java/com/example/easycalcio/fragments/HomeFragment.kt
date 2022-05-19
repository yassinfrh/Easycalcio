package com.example.easycalcio.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.easycalcio.R
import com.example.easycalcio.activities.NewMatchActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //TODO: display the matches

        //listener for the floatingActionButton
        val floatingActionButton : FloatingActionButton = requireView().findViewById(R.id.createMatchButton)
        floatingActionButton.setOnClickListener(object: View.OnClickListener{
            override fun onClick(view: View?) {
                val intent = Intent(view!!.context, NewMatchActivity::class.java)
                view.context.startActivity(intent)
            }
        })
    }

}