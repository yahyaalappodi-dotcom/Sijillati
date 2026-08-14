package com.yahya.sijillati

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class TransactionSummary(
    val totalBalance: Double,
    val cashBalance: Double,
    val cardBalance: Double,
    val totalIncome: Double,
    val totalExpense: Double,
    val netDebt: Double
)

class TransactionManager(context: Context) {

    private val prefs = context.getSharedPreferences("transactions", Context.MODE_PRIVATE)
    private val key = "items"

    fun getAllTransactions(includeArchived: Boolean = false): List<Transaction> {
        val json = prefs.getString(key, "[]") ?: "[]"
        val array = JSONArray(json)
        val list = mutableListOf<Transaction>()

        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)

            val transaction = Transaction(
                id = obj.getInt("id"),
                title = obj.getString("title"),
                amount = obj.getDouble("amount"),
                type = obj.getString("type"),
                paymentMethod = obj.getString("paymentMethod"),
                currency = obj.optString("currency", "د.ع"),
                date = obj.getString("date"),
                isArchived = obj.optBoolean("isArchived", false)
            )

            if (includeArchived || !transaction.isArchived) {
                list.add(transaction)
            }
        }

        return list
    }

    fun addTransaction(transaction: Transaction) {
        val list = getAllTransactions(true).toMutableList()
        list.add(transaction)
        saveAll(list)
    }

    fun updateTransaction(transaction: Transaction) {
        val list = getAllTransactions(true).toMutableList()
        val index = list.indexOfFirst { it.id == transaction.id }

        if (index >= 0) {
            list[index] = transaction
            saveAll(list)
        }
    }

    fun deleteTransaction(id: Int) {
        val list = getAllTransactions(true)
            .filter { it.id != id }

        saveAll(list)
    }

    fun archiveTransaction(id: Int) {
        val list = getAllTransactions(true).map {
            if (it.id == id) {
                it.copy(isArchived = true)
            } else {
                it
            }
        }

        saveAll(list)
    }

    fun getSummary(currency: String = "د.ع"): TransactionSummary {
        val transactions = getAllTransactions(false)
            .filter { it.currency == currency }

        var totalIncome = 0.0
        var totalExpense = 0.0
        var cashBalance = 0.0
        var cardBalance = 0.0
        var netDebt = 0.0

        for (t in transactions) {
            when (t.type) {
                "دخل" -> {
                    totalIncome += t.amount

                    if (t.paymentMethod == "كاش") {
                        cashBalance += t.amount
                    } else {
                        cardBalance += t.amount
                    }
                }

                "مصروف" -> {
                    totalExpense += t.amount

                    if (t.paymentMethod == "كاش") {
                        cashBalance -= t.amount
                    } else {
                        cardBalance -= t.amount
                    }
                }

                "إقراض" -> {
                    netDebt -= t.amount
                }

                "اقتراض" -> {
                    netDebt += t.amount
                }
            }
        }

        return TransactionSummary(
            totalBalance = totalIncome - totalExpense,
            cashBalance = cashBalance,
            cardBalance = cardBalance,
            totalIncome = totalIncome,
            totalExpense = totalExpense,
            netDebt = netDebt
        )
    }

    private fun saveAll(list: List<Transaction>) {
        val array = JSONArray()

        for (t in list) {
            val obj = JSONObject().apply {
                put("id", t.id)
                put("title", t.title)
                put("amount", t.amount)
                put("type", t.type)
                put("paymentMethod", t.paymentMethod)
                put("currency", t.currency)
                put("date", t.date)
                put("isArchived", t.isArchived)
            }

            array.put(obj)
        }

        prefs.edit()
            .putString(key, array.toString())
            .apply()
    }
}
