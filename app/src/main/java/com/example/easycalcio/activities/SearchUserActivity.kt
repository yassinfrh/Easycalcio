package com.example.easycalcio.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.commit
import com.example.easycalcio.R
import com.example.easycalcio.fragments.SearchUserFragment
import com.example.easycalcio.fragments.UserNotFoundFragment
import com.example.easycalcio.fragments.UsersListFragment

class SearchUserActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_user)
        title = "Search user"

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.search_bar, menu)
        if (menu != null) {
            val menuItem: MenuItem = menu.findItem(R.id.actionSearch)
            val searchView: SearchView = menuItem.actionView as SearchView
            val fragmentManager = this.supportFragmentManager
            fragmentManager.commit {
                setReorderingAllowed(true)
                val frag = SearchUserFragment()
                replace(R.id.usersSearchFragment, frag)
            }
            searchView.queryHint = "Search user.."
            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    if (newText != null && newText.isNotEmpty()) {
                        fragmentManager.commit {
                            setReorderingAllowed(true)

                            val frag = UsersListFragment.newInstance(newText)
                            replace(R.id.usersSearchFragment, frag)
                        }
                        return true
                    }
                    fragmentManager.commit {
                        setReorderingAllowed(true)
                        val frag = SearchUserFragment()
                        replace(R.id.usersSearchFragment, frag)
                    }
                    return false
                }

            })
            return super.onCreateOptionsMenu(menu)
        }
        return false
    }
}