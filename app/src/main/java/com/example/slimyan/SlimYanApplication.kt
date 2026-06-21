package com.example.slimyan

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.example.slimyan.work.NotificationWorker
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class SlimYanApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        createReminderChannel()
    }

    private fun createReminderChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NotificationWorker.CHANNEL_ID,
                "リマインド",
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply { description = "食事・筋トレのリマインド通知" }
            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(channel)
        }
    }
}
