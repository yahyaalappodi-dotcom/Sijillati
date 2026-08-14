package com.yahya.sijillati

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.yahya.sijillati.database.AppDatabase
import com.yahya.sijillati.databinding.ActivityReportsBinding

class ReportsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityReportsBinding
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReportsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        db = AppDatabase.getDatabase(this)
        observeData()
    }

    private fun observeData() {
        db.transactionDao().getTotalIncome("IQD").observe(this) { inc ->
            db.transactionDao().getTotalExpense("IQD").observe(this) { exp ->
                db.transactionDao().getTotalLent("IQD").observe(this) { lent ->
                    db.transactionDao().getTotalBorrowed("IQD").observe(this) { borrow ->
                        val income = inc ?: 0.0
                        val expense = exp ?: 0.0
                        val l = lent ?: 0.0
                        val b = borrow ?: 0.0
                        binding.tvTotalIncome.text = "الدخل الإجمالي: %,.0f د.ع".format(income)
                        binding.tvTotalExpense.text = "إجمالي النفقات: %,.0f د.ع".format(expense)
                        binding.tvTotalLent.text = "الإقراض: %,.0f د.ع".format(l)
                        binding.tvTotalBorrowed.text = "الاقتراض: %,.0f د.ع".format(b)
                        setupPieChart(income, expense, l, b)
                    }
                }
            }
        }
    }

    private fun setupPieChart(inc: Double, exp: Double, lent: Double, borrow: Double) {
        val entries = ArrayList<PieEntry>()
        if (inc > 0) entries.add(PieEntry(inc.toFloat(), "دخل"))
        if (exp > 0) entries.add(PieEntry(exp.toFloat(), "مصروف"))
        if (lent > 0) entries.add(PieEntry(lent.toFloat(), "إقراض"))
        if (borrow > 0) entries.add(PieEntry(borrow.toFloat(), "اقتراض"))
        val dataSet = PieDataSet(entries, "")
        dataSet.colors = listOf(Color.parseColor("#4CAF50"), Color.parseColor("#F44336"), Color.parseColor("#FF9800"), Color.parseColor("#9C27B0"))
        dataSet.valueTextSize = 14f
        val data = PieData(dataSet)
        binding.pieChart.data = data
        binding.pieChart.description.isEnabled = false
        binding.pieChart.centerText = "توزيع المعاملات"
        binding.pieChart.animateY(1000)
        binding.pieChart.invalidate()
    }
}
