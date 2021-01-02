package com.example.food2you.ui

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI.setupWithNavController
import androidx.navigation.ui.onNavDestinationSelected
import com.example.food2you.R
import com.example.food2you.databinding.ActivityMainBinding
import com.example.food2you.other.Constants.KEY_EMAIL
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

//    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    @Inject lateinit var sharedPrefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(R.layout.activity_main)

        navController = Navigation.findNavController(this, R.id.navHostFragment)
        setupWithNavController(
            findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar),
            navController
        )

        val email = sharedPrefs.getString(KEY_EMAIL, "") ?: ""

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        toolbar.subtitle = email
        toolbar.inflateMenu(R.menu.main_screen_toolbar)
        toolbar.setOnMenuItemClickListener {
            when(it.itemId) {
                R.id.likeImg -> {
                    navController.navigate(R.id.action_launch_fav_restaurants_fragment)
                }
                R.id.profileImg -> {
                    if(email.isNotEmpty()) {
                        navController.navigate(R.id.action_launch_my_account_fragment)
                    }
                    else {
                        navController.navigate(R.id.action_launch_auth_fragment)
                    }
                }
            }

            it.onNavDestinationSelected(navController)
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when(destination.id) {
                R.id.authFragment -> toolbar.visibility = View.GONE
                else -> toolbar.visibility = View.VISIBLE
            }
        }


    }


    override fun onSupportNavigateUp(): Boolean {
        navController.navigateUp()

        return super.onSupportNavigateUp()
    }

}