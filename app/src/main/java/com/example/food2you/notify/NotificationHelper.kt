package com.example.food2you.notify

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.food2you.R
import com.example.food2you.other.Constants.Add_Preview_Action
import com.example.food2you.other.Constants.KEY_RESTAURANT_ID
import com.example.food2you.ui.MainActivity

class NotificationHelper(context: Context): ContextWrapper(context) {

    private val chanelId = "chanel_id"
    private var notificationManager: NotificationManager? = null


    init {
        createChanel()
    }


    private fun createChanel() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val chanel = NotificationChannel(chanelId, "notificationChanel", NotificationManager.IMPORTANCE_DEFAULT)

            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(chanel)
        }
    }

    fun getManager(): NotificationManager {
        if(notificationManager == null) {
            notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        }

        return notificationManager as NotificationManager
    }


    fun createNotification(notificationId: Long, restaurantId: String, title: String): NotificationCompat.Builder{

       val intent = Intent(applicationContext, MainActivity::class.java).also {
           it.action = Add_Preview_Action
           it.putExtra(KEY_RESTAURANT_ID, restaurantId)
       }


        val vibrateArray = longArrayOf(1000, 1500, 1000)

        return NotificationCompat.Builder(this, chanelId)
                .setAutoCancel(true)
                .setOngoing(true)
                .setContentTitle(title)
                .setContentText("Tap to rate your order")
                .setSmallIcon(R.drawable.my_restaurants_img)
                .setContentIntent(mainActivityPendingIntent(intent, notificationId.toInt() + 1))
                .setVibrate(vibrateArray)
    }

    private fun mainActivityPendingIntent(intent: Intent, requestCode: Int): PendingIntent = PendingIntent.getActivity(applicationContext,
            requestCode,
            intent,
            0
    )


}