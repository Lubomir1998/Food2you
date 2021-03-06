package com.example.food2you.ui

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI.setupWithNavController
import androidx.navigation.ui.onNavDestinationSelected
import com.example.food2you.NavGraphDirections
import com.example.food2you.R
import com.example.food2you.Repository
import com.example.food2you.data.remote.UserToken
import com.example.food2you.other.BasicAuthInterceptor
import com.example.food2you.other.Constants.Add_Preview_Action
import com.example.food2you.other.Constants.KEY_EMAIL
import com.example.food2you.other.Constants.KEY_PASSWORD
import com.example.food2you.other.Constants.KEY_RESTAURANT_ID
import com.example.food2you.other.Constants.KEY_TOKEN
import com.example.food2you.other.Constants.NO_EMAIL
import com.example.food2you.other.Constants.NO_PASSWORD
import com.google.firebase.iid.FirebaseInstanceId
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    @Inject lateinit var sharedPrefs: SharedPreferences
    @Inject lateinit var repository: Repository
    @Inject lateinit var basicAuthInterceptor: BasicAuthInterceptor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_Food2you)
        setContentView(R.layout.activity_main)


        FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener {
            sharedPrefs.edit().putString(KEY_TOKEN, it.token).apply()

            val email = sharedPrefs.getString(KEY_EMAIL, "") ?: ""

            if(email.isNotEmpty()) {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        repository.registerUserToken(UserToken(it.token), email)
                    } catch (e: Exception) { }
                }
            }
        }


        navController = Navigation.findNavController(this, R.id.navHostFragment)
        setupWithNavController(
            findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar),
            navController
        )

        if(intent.action == Add_Preview_Action) {
            goToAddPreviewFragmentIfNotificationIsTapped()
        }

        val email = sharedPrefs.getString(KEY_EMAIL, "") ?: ""

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        toolbar.subtitle = if(email != NO_EMAIL) {
            email
        }
        else {
            ""
        }
        toolbar.inflateMenu(R.menu.main_screen_toolbar)
        toolbar.setOnMenuItemClickListener {
            when(it.itemId) {
                R.id.likeImg -> {
                    navController.navigate(R.id.action_launch_fav_restaurants_fragment)
                }
                R.id.profileImg -> {
                    if(email == NO_EMAIL || email.isEmpty()) {
                        navController.navigate(R.id.action_launch_auth_fragment)
                    }
                    else {
                        navController.navigate(R.id.action_launch_my_account_fragment)
                    }
                }
            }

            it.onNavDestinationSelected(navController)
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when(destination.id) {
                R.id.authFragment -> toolbar.visibility = View.GONE
                R.id.orderFragment -> toolbar.visibility = View.GONE
                else -> toolbar.visibility = View.VISIBLE
            }
        }


    }


    override fun onSupportNavigateUp(): Boolean {
        navController.navigateUp()

        return super.onSupportNavigateUp()
    }


    private fun goToAddPreviewFragmentIfNotificationIsTapped() {
        val email = sharedPrefs.getString(KEY_EMAIL, NO_EMAIL) ?: NO_EMAIL
        val password = sharedPrefs.getString(KEY_PASSWORD, NO_PASSWORD) ?: NO_PASSWORD

        if(email != NO_EMAIL && password != NO_PASSWORD) {
            basicAuthInterceptor.email = email
            basicAuthInterceptor.password = password
        }

        val restaurantId = intent?.getStringExtra(KEY_RESTAURANT_ID) ?: ""
        val action = NavGraphDirections.actionLaunchAddPreviewFragment(restaurantId)
        navController.navigate(action)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        if(intent?.action == Add_Preview_Action) {
            goToAddPreviewFragmentIfNotificationIsTapped()
        }
    }

}