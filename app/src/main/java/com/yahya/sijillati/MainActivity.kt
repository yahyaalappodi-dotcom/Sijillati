package com.yahya.sijillati

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var manager: TransactionManager
    private lateinit var recyclerView: RecyclerView
    private lateinit var tvBalance: TextView
    private lateinit var tvCash: TextView
    private lateinit var tvCard: TextView
    private lateinit var tvIncome: TextView
    private lateinit var tvExpense: TextView
    private lateinit var tvDebtGiven: TextView
    private lateinit var tvDebtTaken: TextView
    private lateinit var btnCurrency: Button
    private var currentCurrency = "د.ع"
    private var unlocked = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        manager = TransactionManager(this)

        tvBalance = findViewById(R.id.tvBalance)
        tvCash = findViewById(R.id.tvCash)
        tvCard = findViewById(R.id.tvCard)
        tvIncome = findViewById(R.id.tvIncome)
        tvExpense = findViewById(R.id.tvExpense)
        tvDebtGiven = findViewById(R.id.tvDebtGiven)
        tvDebtTaken = findViewById(R.id.tvDebtTaken)
        btnCurrency = findViewById(R.id.btnCurrency)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        findViewById<FloatingActionButton>(R.id.fabAdd).setOnClickListener {
            startActivity(Intent(this, AddTransactionActivity::class.java))
        }
        findViewById<Button>(R.id.btnLog).setOnClickListener {
            startActivity(Intent(this, TransactionLogActivity::class.java))
        }
        findViewById<Button>(R.id.btnLog).setOnLongClickListener {
            showMonthlyHistory(); true
        }
        findViewById<Button>(R.id.btnSettings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        findViewById<Button>(R.id.btnReports).setOnClickListener {
            startActivity(Intent(this, ReportsActivity::class.java))
        }
        btnCurrency.setOnClickListener {
            currentCurrency = if (currentCurrency == "د.ع") "$" else "د.ع"
            btnCurrency.text = "💱 $currentCurrency"
            refresh()
        }

        checkBiometricLock()
    }

    private fun checkBiometricLock() {
        val prefs = getSharedPreferences("sijillati_prefs", MODE_PRIVATE)
        val enabled = prefs.getBoolean("biometric_lock", false)
        if (enabled && !unlocked) {
            BiometricHelper.authenticate(this, onSuccess = { unlocked = true }, onFail = { finish() })
        } else {
            unlocked = true
        }
    }

    override fun onResume() {
        super.onResume()
        manager.checkMonthlyRollover()
        checkBiometricLock()
        refresh()
    }

    private fun refresh() {
        recyclerView.adapter = TransactionAdapter(manager.getAllTransactions().filter { it.currency == currentCurrency }) { t -> showLongPressMenu(t) }
        val s = manager.getSummary(currentCurrency)
        tvBalance.text = money(s.totalBalance)
        tvCash.text = money(s.cashBalance)
        tvCard.text = money(s.cardBalance)
        tvIncome.text = money(s.totalIncome)
        tvExpense.text = money(s.totalExpense)
        tvDebtGiven.text = money(s.debtGivenTotal)
        tvDebtTaken.text = money(s.debtTakenTotal)
    }

    private fun money(x: Double) = String.format(Locale.US, "%,.0f %s", x, currentCurrency)

    private fun showMonthlyHistory() {
        val history = manager.getMonthlyHistory()
        if (history.isEmpty()) {
            AlertDialog.Builder(this).setTitle("السجل الشهري").setMessage("لا يوجد سجل شهري بعد").setPositiveButton("إغلاق", null).show()
            return
        }
        val items = history.map { String.format(Locale.US, "%s: مصروف %,.0f | دخل %,.0f | الصافي %,.0f", it.month, it.totalExpense, it.totalIncome, it.remaining) }.toTypedArray()
        AlertDialog.Builder(this).setTitle("السجل الشهري").setItems(items, null).setPositiveButton("إغلاق", null).show()
    }

    private fun showLongPressMenu(t: Transaction) {
        AlertDialog.Builder(this)
            .setTitle(t.title)
            .setItems(arrayOf("➖ حذف", "✏️ تعديل", "📦 أرشفة")) { _, which ->
                when (which) {
                    0 -> confirmDelete(t)
                    1 -> startActivity(Intent(this, AddTransactionActivity::class.java).apply { putExtra("EXTRA_ID", t.id) })
                    2 -> confirmArchive(t)
                }
            }.show()
    }

    private fun confirmDelete(t: Transaction) {
        AlertDialog.Builder(this)
            .setTitle("حذف المعاملة")
            .setMessage("هل تريد حذف '${t.title}' نهائياً؟ لا يمكن التراجع.")
            .setPositiveButton("حذف") { _, _ -> manager.deleteTransaction(t.id); refresh() }
            .setNegativeButton("إلغاء", null).show()
    }

    private fun confirmArchive(t: Transaction) {
        AlertDialog.Builder(this)
            .setTitle("أرشفة المعاملة")
            .setMessage("هل تريد نقل '${t.title}' إلى الأرشيف؟")
            .setPositiveButton("أرشفة") { _, _ -> manager.archiveTransaction(t.id); refresh() }
            .setNegativeButton("إلغاء", null).show()
    }
}
