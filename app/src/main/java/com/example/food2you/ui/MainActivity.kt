package com.example.food2you.ui

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI.setupWithNavController
import androidx.navigation.ui.onNavDestinationSelected
import com.example.food2you.NavGraphDirections
import com.example.food2you.R
import com.example.food2you.Repository
import com.example.food2you.data.remote.PushNotification
import com.example.food2you.other.Constants
import com.example.food2you.other.Constants.Add_Preview_Action
import com.example.food2you.other.Constants.KEY_EMAIL
import com.example.food2you.other.Constants.KEY_RESTAURANT_ID
import com.example.food2you.service.FirebaseService
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.log

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    @Inject lateinit var sharedPrefs: SharedPreferences
    @Inject lateinit var repository: Repository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_Food2you)
        setContentView(R.layout.activity_main)


        FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener {
            val email = sharedPrefs.getString(KEY_EMAIL, "") ?: ""
            val pushNotification = PushNotification("On the way!", "Your order is currently on the way", it.token)
            sharedPrefs.edit().putString(Constants.KEY_TOKEN, it.token).apply()

            if(email.isNotEmpty()) {
                CoroutineScope(Dispatchers.IO).launch {
                    repository.registerUserToken(pushNotification, email)
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