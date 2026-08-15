package com.yahya.sijillati

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class LogEntry(val action: String, val details: String, val timestamp: String)
data class MonthlyRecord(val month: String, val totalExpense: Double, val totalIncome: Double, val remaining: Double)

class TransactionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("sijillati_prefs", Context.MODE_PRIVATE)
    private val key = "transactions"
    private val logKey = "activity_log"
    private val historyKey = "monthly_history"
    private val lastCheckKey = "last_check_month"

    fun addTransaction(t: Transaction) {
        val list = getAllTransactions(true).toMutableList()
        list.add(0, t); saveList(list)
        log("اضافة معاملة", t.title + " (" + money(t.amount) + " " + t.currency + ") - " + t.type)
    }

    fun updateTransaction(t: Transaction) {
        val list = getAllTransactions(true).toMutableList()
        val i = list.indexOfFirst { it.id == t.id }
        if (i != -1) {
            list[i] = t; saveList(list)
            log("تعديل معاملة", t.title)
        }
    }

    fun archiveTransaction(id: Int) {
        val list = getAllTransactions(true).toMutableList()
        val i = list.indexOfFirst { it.id == id }
        if (i != -1) {
            list[i] = list[i].copy(isArchived = true); saveList(list)
            log("ارشفة معاملة", list[i].title)
        }
    }

    fun deleteTransaction(id: Int) {
        val list = getAllTransactions(true).toMutableList()
        val i = list.indexOfFirst { it.id == id }
        if (i != -1) {
            val t = list[i]
            list.removeAt(i); saveList(list)
            log("حذف معاملة", t.title)
        }
    }

    fun getAllTransactions(includeArchived: Boolean = false): List<Transaction> {
        val a = JSONArray(prefs.getString(key, "[]") ?: "[]")
        val list = mutableListOf<Transaction>()
        for (i in 0 until a.length()) {
            val o = a.getJSONObject(i)
            val archived = o.optBoolean("isArchived", false)
            if (!includeArchived && archived) continue
            list.add(Transaction(
                o.getInt("id"), o.optString("title"), o.optDouble("amount", 0.0),
                o.optString("type"), o.optString("paymentMethod"),
                o.optString("currency", "IQD"), o.optString("date"), archived
            ))
        }
        return list
    }


    fun getSummary(currency: String): Summary {
        var cash = 0.0
        var card = 0.0
        var income = 0.0
        var expense = 0.0
        var debtGiven = 0.0
        var debtTaken = 0.0
        val filtered = getAllTransactions(false).filter { it.currency == currency }
        for (t in filtered) {
            val isCash = t.paymentMethod == "كاش"
            when (t.type) {
                "دخل" -> { income += t.amount; if (isCash) cash += t.amount else card += t.amount }
                "مصروف" -> { expense += t.amount; if (isCash) cash -= t.amount else card -= t.amount }
                "اقتراض" -> { debtTaken += t.amount; if (isCash) cash += t.amount else card += t.amount }
                "إقراض" -> { debtGiven += t.amount; if (isCash) cash -= t.amount else card -= t.amount }
            }
        }
        val netDebt = debtTaken - debtGiven
        return Summary(cash, card, cash + card, income, expense, debtGiven, debtTaken, netDebt)
    }

    fun checkMonthlyRollover() {
        val sdfMonth = SimpleDateFormat("yyyy-MM", Locale.US)
        val currentMonth = sdfMonth.format(Date())
        val lastMonth = prefs.getString(lastCheckKey, null)
        if (lastMonth == null) {
            prefs.edit().putString(lastCheckKey, currentMonth).apply()
            return
        }
        if (lastMonth == currentMonth) return
        val monthTxns = getAllTransactions(false).filter { it.date.startsWith(lastMonth) }
        if (monthTxns.isNotEmpty()) {
            var income = 0.0
            var expense = 0.0
            for (t in monthTxns) {
                when (t.type) {
                    "دخل" -> income += t.amount
                    "مصروف" -> expense += t.amount
                }
            }
            val history = getMonthlyHistory().toMutableList()
            history.add(0, MonthlyRecord(lastMonth, expense, income, income - expense))
            saveMonthlyHistory(history)
            val allList = getAllTransactions(true).toMutableList()
            for (mt in monthTxns) {
                val idx = allList.indexOfFirst { it.id == mt.id }
                if (idx != -1) allList[idx] = allList[idx].copy(isArchived = true)
            }
            saveList(allList)
            log("تصفير شهري", "تم ارشفة معاملات شهر " + lastMonth)
        }
        prefs.edit().putString(lastCheckKey, currentMonth).apply()
    }

    fun getMonthlyHistory(): List<MonthlyRecord> {
        val a = JSONArray(prefs.getString(historyKey, "[]") ?: "[]")
        val list = mutableListOf<MonthlyRecord>()
        for (i in 0 until a.length()) {
            val o = a.getJSONObject(i)
            list.add(MonthlyRecord(o.getString("month"), o.getDouble("totalExpense"), o.getDouble("totalIncome"), o.getDouble("remaining")))
        }
        return list
    }

    private fun saveMonthlyHistory(list: List<MonthlyRecord>) {
        val a = JSONArray()
        for (r in list) {
            val o = JSONObject()
            o.put("month", r.month)
            o.put("totalExpense", r.totalExpense)
            o.put("totalIncome", r.totalIncome)
            o.put("remaining", r.remaining)
            a.put(o)
        }
        prefs.edit().putString(historyKey, a.toString()).apply()
    }

    fun getActivityLog(): List<LogEntry> {
        val a = JSONArray(prefs.getString(logKey, "[]") ?: "[]")
        val list = mutableListOf<LogEntry>()
        for (i in 0 until a.length()) {
            val o = a.getJSONObject(i)
            list.add(LogEntry(o.getString("action"), o.getString("details"), o.getString("timestamp")))
        }
        return list
    }

    private fun log(action: String, details: String) {
        val list = getActivityLog().toMutableList()
        val ts = SimpleDateFormat("HH:mm:ss dd-MM-yyyy", Locale.US).format(Date())
        list.add(0, LogEntry(action, details, ts))
        val a = JSONArray()
        for (e in list) {
            val o = JSONObject()
            o.put("action", e.action)
            o.put("details", e.details)
            o.put("timestamp", e.timestamp)
            a.put(o)
        }
        prefs.edit().putString(logKey, a.toString()).apply()
    }

    private fun money(x: Double) = String.format(Locale.US, "%,.0f", x)

    private fun saveList(list: List<Transaction>) {
        val a = JSONArray()
        for (t in list) {
            val o = JSONObject()
            o.put("id", t.id)
            o.put("title", t.title)
            o.put("amount", t.amount)
            o.put("type", t.type)
            o.put("paymentMethod", t.paymentMethod)
            o.put("currency", t.currency)
            o.put("date", t.date)
            o.put("isArchived", t.isArchived)
            a.put(o)
        }
        prefs.edit().putString(key, a.toString()).apply()
    }

    data class Summary(
        val cashBalance: Double,
        val cardBalance: Double,
        val totalBalance: Double,
        val totalIncome: Double,
        val totalExpense: Double,
        val debtGivenTotal: Double,
        val debtTakenTotal: Double,
        val netDebt: Double
    )
}
