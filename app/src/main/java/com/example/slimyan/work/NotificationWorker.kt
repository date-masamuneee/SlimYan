package com.example.slimyan.work

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.slimyan.R

/**
 * リマインド通知を表示し、次回発火を自身で再スケジュールする Worker。
 * 必要な情報はすべて inputData で受け取り DB に依存しない（Hilt不要）。
 */
class NotificationWorker(
    context: Context,
    params: WorkerParameters,
) : Worker(context, params) {

    override fun doWork(): Result {
        val id = inputData.getLong(KEY_ID, 0L)
        val label = inputData.getString(KEY_LABEL) ?: "リマインド"
        val mask = inputData.getInt(KEY_MASK, 0)
        val hour = inputData.getInt(KEY_HOUR, 0)
        val minute = inputData.getInt(KEY_MINUTE, 0)

        showNotification(id, label)

        // 次回を再スケジュール
        ReminderScheduler.scheduleRaw(applicationContext, id, label, mask, hour, minute)
        return Result.success()
    }

    private fun showNotification(id: Long, label: String) {
        val ctx = applicationContext
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            return // 権限が無ければ静かにスキップ
        }
        val notification = NotificationCompat.Builder(ctx, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("SlimYan")
            .setContentText(label)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(id.toInt(), notification)
    }

    companion object {
        const val CHANNEL_ID = "slimyan_reminders"
        const val KEY_ID = "id"
        const val KEY_LABEL = "label"
        const val KEY_MASK = "mask"
        const val KEY_HOUR = "hour"
        const val KEY_MINUTE = "minute"
    }
}
