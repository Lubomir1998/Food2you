package com.example.food2you.other

import android.Manifest
import com.example.food2you.BuildConfig

object Constants {

    const val ENCRYPTED_SHARED_PREFS_NAME = "ENCRYPTED_SHARED_PREFS_NAME"

    const val KEY_EMAIL = "KEY_EMAIL"
    const val KEY_PASSWORD = "KEY_PASSWORD"

    const val NO_EMAIL = "NO_EMAIL"
    const val NO_PASSWORD = "NO_PASSWORD"

    const val KEY_ADDRESS = "KEY_ADDRESS"
    const val KEY_PHONE = "KEY_PHONE"

    const val KEY_RESTAURANT = "KEY_RESTAURANT"

    const val KEY_TIMESTAMP = "KEY_TIMESTAMP"
    const val KEY_RESTAURANT_ID = "KEY_RESTAURANT_ID"

    const val Add_Preview_Action = "Add_Preview_Action"

    const val KEY_TOKEN = "KEY_TOKEN"

    const val SERVER_KEY = BuildConfig.SERVER_KEY
    const val CONTENT_TYPE = "application/json"

    const val CHANNEL_ID = "CHANNEL_ID"

    const val LOCATION_PERMISSION_REQUEST_CODE = 1
    const val FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION
    const val COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION

}