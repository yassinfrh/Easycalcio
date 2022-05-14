package com.example.easycalcio.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.example.easycalcio.R
import com.example.easycalcio.activities.LoginActivity
import com.example.easycalcio.models.FirebaseWrapper

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [LogFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
abstract class LogFragment(val label: String, val buttonLabel: String) : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view : View =  inflater.inflate(R.layout.fragment_log, container, false)

        val thiz = this
        val link : Button = view.findViewById(R.id.switchLoginRegisterButton)
        link.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                (thiz.requireActivity() as LoginActivity).switchFragment()
            }
        })

        val button : Button = view.findViewById(R.id.logButton)
        button.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                val email : EditText = thiz.requireActivity().findViewById(R.id.userEmail)
                val password : EditText = thiz.requireActivity().findViewById(R.id.userPassword)

                if (email.text.isEmpty() || password.text.isEmpty()) {
                    email.setError("This is required")
                    password.setError("This is required")
                    return
                }

                action(email.text.toString(), password.text.toString())
            }

        })

        view.findViewById<TextView>(R.id.switchLoginRegisterButton).setText(this.label)
        view.findViewById<Button>(R.id.logButton).setText(this.buttonLabel)

        return view
    }

    abstract fun action(email: String, password: String);

}

// TODO: Remove hardcoded strings and put them in the file res/values/strings and retrieve them with the R.string.<string_id>
class LogInFragment : LogFragment("Switch to SignUp","LOGIN") {
    override fun action(email: String, password: String) {
        val firebaseWrapper : FirebaseWrapper = FirebaseWrapper(this.requireContext())
        firebaseWrapper.signIn(email, password)
    }

    /*override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_log, container, false)
    }*/
}

class SignUpFragment : LogFragment("Switch to LogIn", "SIGNUP") {
    override fun action(email: String, password: String) {
        val firebaseWrapper : FirebaseWrapper = FirebaseWrapper(this.requireContext())
        firebaseWrapper.signUp(email, password)
    }

    /*override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_log, container, false)
    }*/
}