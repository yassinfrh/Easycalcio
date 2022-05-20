package com.example.easycalcio.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.widget.SearchView
import com.example.easycalcio.R

class SearchUserActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_user)
        title = "Search user"

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.search_bar, menu)
        if (menu != null){
            val menuItem : MenuItem = menu.findItem(R.id.actionSearch)
            val searchView : SearchView = menuItem.actionView as SearchView
            searchView.queryHint = "Search user.."
            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    //TODO: search users
                    return false
                }

            })
            return super.onCreateOptionsMenu(menu)
        }
        return false
    }
}