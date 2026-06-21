package com.example.slimyan.work

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.slimyan.data.ReminderTiming
import com.example.slimyan.data.entity.Reminder
import java.time.Duration
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

/** リマインドの WorkManager スケジュール／キャンセルを担当。 */
@Singleton
class ReminderScheduler @Inject constructor() {

    fun schedule(context: Context, reminder: Reminder) {
        if (!reminder.enabled) {
            cancel(context, reminder.id)
            return
        }
        scheduleRawStatic(context, reminder.id, reminder.label, reminder.daysOfWeekMask, reminder.hour, reminder.minute)
    }

    fun cancel(context: Context, id: Long) {
        WorkManager.getInstance(context).cancelUniqueWork(workName(id))
    }

    companion object {
        private fun workName(id: Long) = "reminder_$id"

        /** Worker からの再スケジュール用（インスタンス不要）。 */
        fun scheduleRaw(context: Context, id: Long, label: String, mask: Int, hour: Int, minute: Int) {
            scheduleRawStatic(context, id, label, mask, hour, minute)
        }

        private fun scheduleRawStatic(context: Context, id: Long, label: String, mask: Int, hour: Int, minute: Int) {
            val next = ReminderTiming.nextTrigger(mask, hour, minute, LocalDateTime.now()) ?: return
            val delay = Duration.between(LocalDateTime.now(), next).coerceAtLeast(Duration.ofSeconds(1))

            val data = Data.Builder()
                .putLong(NotificationWorker.KEY_ID, id)
                .putString(NotificationWorker.KEY_LABEL, label)
                .putInt(NotificationWorker.KEY_MASK, mask)
                .putInt(NotificationWorker.KEY_HOUR, hour)
                .putInt(NotificationWorker.KEY_MINUTE, minute)
                .build()

            val request = OneTimeWorkRequestBuilder<NotificationWorker>()
                .setInitialDelay(delay)
                .setInputData(data)
                .addTag(workName(id))
                .build()

            WorkManager.getInstance(context)
                .enqueueUniqueWork(workName(id), ExistingWorkPolicy.REPLACE, request)
        }
    }
}
