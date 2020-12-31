package com.example.food2you.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI.setupWithNavController
import androidx.navigation.ui.onNavDestinationSelected
import com.example.food2you.R
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        navController = Navigation.findNavController(this, R.id.navHostFragment)
        setupWithNavController(
            findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar),
            navController
        )

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        toolbar.inflateMenu(R.menu.main_screen_toolbar)
        toolbar.setOnMenuItemClickListener {
//            Toast.makeText(this, "hahahahahahahaha", Toast.LENGTH_SHORT).show()
            it.onNavDestinationSelected(navController)
        }


    }


    override fun onSupportNavigateUp(): Boolean {
        navController.navigateUp()

        return super.onSupportNavigateUp()
    }

}