package com.yahya.sijillati

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat

class NotificationHelper(private val context: Context) {
    companion object {
        const val CHANNEL_ID = "daily_reminder"
        const val NOTIFICATION_ID = 1001
    }
    fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "التذكير اليومي", NotificationManager.IMPORTANCE_DEFAULT)
            channel.description = "إشعار يومي بمصاريفك"
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
    fun showDailyReminder(totalExpenseIqd: Double, totalExpenseUsd: Double) {
        val message = when {
            totalExpenseIqd > 0 && totalExpenseUsd > 0 -> "صرفت اليوم: %,.0f د.ع و %,.0f $".format(totalExpenseIqd, totalExpenseUsd)
            totalExpenseIqd > 0 -> "صرفت اليوم: %,.0f د.ع".format(totalExpenseIqd)
            totalExpenseUsd > 0 -> "صرفت اليوم: %,.0f $".format(totalExpenseUsd)
            else -> "لم تصرف شيئاً اليوم! احسنت 👏"
        }
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("سجلتي - ملخص اليوم")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, notification)
    }
}
