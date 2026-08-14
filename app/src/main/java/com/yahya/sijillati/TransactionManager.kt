package com.yahya.sijillati

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class LogEntry(val action: String, val details: String, val timestamp: String)

class TransactionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("sijillati_prefs", Context.MODE_PRIVATE)
    private val key = "transactions"
    private val logKey = "activity_log"

    fun addTransaction(t: Transaction) {
        val list = getAllTransactions(true).toMutableList()
        list.add(0, t); saveList(list)
        log("إضافة معاملة", "${t.title} (${money(t.amount)} ${t.currency}) - ${t.type}")
    }

    fun updateTransaction(t: Transaction) {
        val list = getAllTransactions(true).toMutableList()
        val i = list.indexOfFirst { it.id == t.id }
        if (i != -1) {
            list[i] = t; saveList(list)
            log("تعديل معاملة", "${t.title} (${money(t.amount)} ${t.currency})")
        }
    }

    fun archiveTransaction(id: Int) {
        val list = getAllTransactions(true).toMutableList()
        val i = list.indexOfFirst { it.id == id }
        if (i != -1) {
            list[i] = list[i].copy(isArchived = true); saveList(list)
            log("أرشفة معاملة", "${list[i].title} (${money(list[i].amount)} ${list[i].currency})")
        }
    }

    fun deleteTransaction(id: Int) {
        val list = getAllTransactions(true).toMutableList()
        val i = list.indexOfFirst { it.id == id }
        if (i != -1) {
            val t = list[i]
            list.removeAt(i); saveList(list)
            log("حذف معاملة", "${t.title} (${money(t.amount)} ${t.currency})")
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
                o.optString("currency", "د.ع"), o.optString("date"), archived
            ))
        }
        return list
    }

    fun getSummary(currency: String): Summary {
        var cash = 0.0; var card = 0.0
        var income = 0.0; var expense = 0.0
        var debtGiven = 0.0; var debtTaken = 0.0

        getAllTransactions(false).filter { it.currency == currency }.forEach { t ->
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
        list.forEach { e -> a.put(JSONObject().apply {
            put("action", e.action); put("details", e.details); put("timestamp", e.timestamp)
        }) }
        prefs.edit().putString(logKey, a.toString()).apply()
    }

    private fun money(x: Double) = String.format(Locale.US, "%,.0f", x)

    private fun saveList(list: List<Transaction>) {
        val a = JSONArray()
        list.forEach { t -> a.put(JSONObject().apply {
            put("id", t.id); put("title", t.title); put("amount", t.amount); put("type", t.type)
            put("paymentMethod", t.paymentMethod); put("currency", t.currency); put("date", t.date)
            put("isArchived", t.isArchived)
        }) }
        prefs.edit().putString(key, a.toString()).apply()
    }

    data class Summary(
        val cashBalance: Double, val cardBalance: Double, val totalBalance: Double,
        val totalIncome: Double, val totalExpense: Double,
        val debtGivenTotal: Double, val debtTakenTotal: Double, val netDebt: Double
    )
}
