package com.yahya.sijillati

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.yahya.sijillati.database.AppDatabase
import java.util.*

class DailyReminderWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val db = AppDatabase.getDatabase(applicationContext)
        val dao = db.transactionDao()
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val startOfDay = cal.timeInMillis
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        val endOfDay = cal.timeInMillis
        val allTransactions = dao.getAll().value ?: emptyList()
        val todayExpenses = allTransactions.filter { it.type == "EXPENSE" && it.date in startOfDay..endOfDay }
        val totalIqd = todayExpenses.filter { it.currency == "IQD" }.sumOf { it.amount }
        val totalUsd = todayExpenses.filter { it.currency == "USD" }.sumOf { it.amount }
        val helper = NotificationHelper(applicationContext)
        helper.createChannel()
        helper.showDailyReminder(totalIqd, totalUsd)
        return Result.success()
    }
}
