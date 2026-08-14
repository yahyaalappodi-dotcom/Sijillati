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

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        findViewById<FloatingActionButton>(R.id.fabAdd).setOnClickListener {
            startActivity(Intent(this, AddTransactionActivity::class.java))
        }
        findViewById<Button>(R.id.btnLog).setOnClickListener {
            startActivity(Intent(this, TransactionLogActivity::class.java))
        }
    }

    override fun onResume() { super.onResume(); refresh() }

    private fun refresh() {
        recyclerView.adapter = TransactionAdapter(manager.getAllTransactions()) { t -> showLongPressMenu(t) }
        val s = manager.getSummary("د.ع")
        tvBalance.text = money(s.totalBalance)
        tvCash.text = money(s.cashBalance)
        tvCard.text = money(s.cardBalance)
        tvIncome.text = money(s.totalIncome)
        tvExpense.text = money(s.totalExpense)
        tvDebtGiven.text = money(s.debtGivenTotal)
        tvDebtTaken.text = money(s.debtTakenTotal)
    }

    private fun money(x: Double) = String.format(Locale.US, "%,.0f د.ع", x)

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
