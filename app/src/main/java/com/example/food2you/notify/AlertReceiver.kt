package com.example.food2you.notify

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import com.example.food2you.other.Constants.KEY_RESTAURANT_ID
import com.example.food2you.other.Constants.KEY_TIMESTAMP

class AlertReceiver: BroadcastReceiver() {

    private lateinit var notificationHelper: NotificationHelper

    @SuppressLint("InvalidWakeLockTag", "UnsafeProtectedBroadcastReceiver")
    override fun onReceive(context: Context?, intent: Intent?) {

        // wake the screen after receiving the notification
        val pm = context!!.getSystemService(Context.POWER_SERVICE) as PowerManager
        val isScreenOn = pm.isScreenOn
        if (!isScreenOn) {
            val wl = pm.newWakeLock(
                    PowerManager.FULL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP or PowerManager.ON_AFTER_RELEASE,
                    "MyLock"
            )
            wl.acquire(10000)
            val wl_cpu = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyCpuLock")
            wl_cpu.acquire(10000)
        }


        notificationHelper = NotificationHelper(context)

        val id = intent?.getLongExtra(KEY_TIMESTAMP, 1) ?: 1
        val restaurantId = intent?.getStringExtra(KEY_RESTAURANT_ID) ?: ""

        val notification = notificationHelper.createNotification(id, restaurantId)
                .build()

        notificationHelper.getManager().notify(id.toInt() + 1, notification)

    }
}