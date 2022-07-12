package com.example.easycalcio.fragments

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.easycalcio.R
import com.example.easycalcio.activities.MainActivity
import com.example.easycalcio.models.*
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*

class ProfileFragment : Fragment() {

    private var edit = false
    private val myCalendar: Calendar = Calendar.getInstance()
    private val roles =
        arrayOf("Goalkeeper", "Center back", "Full back", "Center midfielder", "Wing", "Striker")
    private var profileImage: ImageView? = null
    var currentUsername: String? = null
    var image: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(fragView: View, savedInstanceState: Bundle?) {
        super.onViewCreated(fragView, savedInstanceState)

        val profileChangePictureButton: Button =
            requireView().findViewById(R.id.profileChangePictureButton)
        val profileUsername: EditText = requireView().findViewById(R.id.profileUsername)
        val profileName: EditText = requireView().findViewById(R.id.profileName)
        val profileSurname: EditText = requireView().findViewById(R.id.profileSurname)
        val profileBirthday: EditText = requireView().findViewById(R.id.profileBirthday)
        val profileCity: EditText = requireView().findViewById(R.id.profileCity)
        profileImage = requireView().findViewById(R.id.profileImage)

        val views =
            arrayOf(profileUsername, profileName, profileSurname, profileBirthday, profileCity)

        val roleSpinner: Spinner = requireView().findViewById(R.id.profileRoleSpinner)
        val adapter: ArrayAdapter<CharSequence> = ArrayAdapter.createFromResource(
            fragView.context,
            R.array.roles,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        roleSpinner.adapter = adapter
        roleSpinner.isEnabled = false
        roleSpinner.isClickable = false

        var user: User? = null


        CoroutineScope(Dispatchers.Main + Job()).launch {
            withContext(Dispatchers.IO) {
                user = getUser(requireContext())
                image = FirebaseStorageWrapper().download(user!!.username.lowercase())

                withContext(Dispatchers.Main) {
                    profileUsername.setText(user!!.username)
                    profileName.setText(user!!.name)
                    profileSurname.setText(user!!.surname)
                    profileBirthday.setText(user!!.formattedDate)
                    profileCity.setText(user!!.city)
                    roleSpinner.setSelection(roles.indexOf(user!!.role) + 1)
                    currentUsername = user!!.username.lowercase()
                    if (image != null) {
                        profileImage!!.setImageURI(image)
                    }
                }
            }
        }

        val date =
            DatePickerDialog.OnDateSetListener { view, year, month, day ->
                myCalendar.set(Calendar.YEAR, year)
                myCalendar.set(Calendar.MONTH, month)
                myCalendar.set(Calendar.DAY_OF_MONTH, day)
                val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.US)
                profileBirthday.setText(dateFormat.format(myCalendar.time))
            }
        profileBirthday.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View?) {
                val datePicker = DatePickerDialog(
                    view!!.context,
                    date,
                    myCalendar[Calendar.YEAR],
                    myCalendar[Calendar.MONTH],
                    myCalendar[Calendar.DAY_OF_MONTH]
                )
                datePicker.datePicker.maxDate = System.currentTimeMillis() - 315569260000 //10 years
                if (edit) {
                    datePicker.show()
                }
            }
        })

        var usernameErr = false

        profileUsername.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                usernameErr = false
                val newUsername = profileUsername.text.toString().lowercase()
                CoroutineScope(Dispatchers.Main + Job()).launch {
                    withContext(Dispatchers.IO) {
                        val used = alreadyUsedUsername(
                            view!!.context,
                            newUsername
                        )
                        withContext(Dispatchers.Main) {
                            if (used && newUsername != currentUsername) {
                                usernameErr = true
                                profileUsername.error = "Already used username"
                            }
                        }
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {
            }

        })


        val profileButton: FloatingActionButton =
            requireView().findViewById(R.id.profileEditSaveButton)
        var err: Boolean
        profileButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View?) {
                err = false
                edit = !edit

                if (edit) {
                    profileButton.setImageResource(android.R.drawable.ic_menu_save)
                    profileChangePictureButton.visibility = Button.VISIBLE

                    profileChangePictureButton.setOnClickListener(object : View.OnClickListener {
                        override fun onClick(v: View?) {
                            ImagePicker.with(this@ProfileFragment)
                                .crop()                    //Crop image(Optional), Check Customization for more option
                                .compress(1024)            //Final image size will be less than 1 MB(Optional)
                                .maxResultSize(
                                    500,
                                    500
                                )    //Final image resolution will be less than 1080 x 1080(Optional)
                                .start()
                        }

                    })

                    for (v in views) {
                        if (v != profileBirthday) {
                            v.isClickable = true
                            v.isCursorVisible = true
                            v.isFocusable = true
                            v.isFocusableInTouchMode = true
                        }
                    }
                    roleSpinner.isEnabled = true
                    roleSpinner.isClickable = true
                } else {
                    for (v in views) {
                        v.error = null
                        if (v.text.isEmpty()) {
                            err = true
                            v.error = "Required field"
                        }
                    }
                    if ((roleSpinner.selectedItem as String) == "Select a role..") {
                        err = true
                        Toast.makeText(view!!.context, "Select a role!", Toast.LENGTH_SHORT).show()
                    }
                    if (err || usernameErr)
                        return

                    for (v in views) {
                        if (v != profileBirthday) {
                            v.isClickable = false
                            v.isCursorVisible = false
                            v.isFocusable = false
                            v.isFocusableInTouchMode = false
                        }
                    }
                    roleSpinner.isEnabled = false
                    roleSpinner.isClickable = false

                    profileButton.setImageResource(android.R.drawable.ic_menu_edit)
                    profileChangePictureButton.visibility = Button.GONE

                    val oldUsername = currentUsername
                    currentUsername = profileUsername.text.toString()

                    val formatter = SimpleDateFormat("yyyy/MM/dd")

                    user!!.username = profileUsername.text.toString().lowercase()
                    user!!.name = profileName.text.toString()
                    user!!.surname = profileSurname.text.toString()
                    user!!.birthday = formatter.parse(profileBirthday.text.toString()) as Date
                    user!!.city = profileCity.text.toString()
                    user!!.role = roleSpinner.selectedItem.toString()

                    FirebaseDbWrapper(requireContext()).writeUser(user!!)
                    if (user!!.friends != null) {
                        for (friend in user!!.friends!!) {
                            GlobalScope.launch {
                                val friendUser = getUserWithUsername(view!!.context, friend)
                                friendUser.friends!!.remove(oldUsername!!)
                                friendUser.friends!!.add(currentUsername!!)
                                replaceUser(requireContext(), friend, friendUser)
                            }
                        }
                    }

                    if (user!!.matches != null) {
                        for (matchId in user!!.matches!!) {
                            GlobalScope.launch {
                                val match = getMatch(requireContext(), matchId)
                                if(match.username == oldUsername){
                                    match.username = currentUsername!!
                                }
                                match.players!!.remove(oldUsername)
                                match.players!!.add(currentUsername!!)
                                FirebaseDbWrapper(requireContext()).dbRef.child("matches")
                                    .child(matchId.toString()).setValue(match)
                            }
                        }
                    }

                    GlobalScope.launch {
                        replaceUsernameInMatchRequests(requireContext(), oldUsername!!, currentUsername!!)
                        replaceUsernameInFriendRequests(requireContext(), oldUsername, currentUsername!!)
                        replaceUsernameInChats(requireContext(), oldUsername, currentUsername!!)
                    }

                    if (image != null) {
                        FirebaseStorageWrapper().delete(oldUsername!!)
                        FirebaseStorageWrapper().upload(image!!, currentUsername!!, requireContext())
                        val progressBar : RelativeLayout = requireView().findViewById(R.id.loadingPanel)
                        progressBar.visibility = View.VISIBLE
                        profileButton.isEnabled = false
                    }

                }

            }

        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (resultCode) {
            Activity.RESULT_OK -> {
                //Image Uri will not be null for RESULT_OK
                image = data?.data!!

                // Use Uri object instead of File to avoid storage permissions
                profileImage!!.setImageURI(image)
            }
            ImagePicker.RESULT_ERROR -> {
                Toast.makeText(requireContext(), ImagePicker.getError(data), Toast.LENGTH_SHORT)
                    .show()
            }
            else -> {
                Toast.makeText(requireContext(), "Task Cancelled", Toast.LENGTH_SHORT).show()
            }
        }
    }

}