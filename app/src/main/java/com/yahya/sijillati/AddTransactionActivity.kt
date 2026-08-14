package com.yahya.sijillati

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.yahya.sijillati.database.AppDatabase
import com.yahya.sijillati.database.TransactionEntity
import com.yahya.sijillati.databinding.ActivityAddTransactionBinding
import kotlinx.coroutines.launch
import java.util.*

class AddTransactionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddTransactionBinding
    private lateinit var db: AppDatabase
    private var selectedDate: Long = System.currentTimeMillis()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddTransactionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        db = AppDatabase.getDatabase(this)
        binding.tvDate.text = formatDate(selectedDate)
        binding.btnPickDate.setOnClickListener { showDatePicker() }
        binding.btnSave.setOnClickListener { saveTransaction() }
    }

    private fun showDatePicker() {
        val cal = Calendar.getInstance()
        cal.timeInMillis = selectedDate
        DatePickerDialog(this, { _, y, m, d ->
            cal.set(y, m, d)
            selectedDate = cal.timeInMillis
            binding.tvDate.text = formatDate(selectedDate)
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun saveTransaction() {
        val title = binding.etTitle.text.toString().trim()
        val amountStr = binding.etAmount.text.toString().trim()
        if (title.isEmpty() || amountStr.isEmpty()) {
            Toast.makeText(this, "يرجى ملء جميع الحقول", Toast.LENGTH_SHORT).show()
            return
        }
        val amount = amountStr.toDoubleOrNull() ?: 0.0
        if (amount <= 0) {
            Toast.makeText(this, "المبلغ غير صحيح", Toast.LENGTH_SHORT).show()
            return
        }
        val type = when (binding.rgType.checkedRadioButtonId) {
            R.id.rbIncome -> "INCOME"
            R.id.rbExpense -> "EXPENSE"
            R.id.rbLend -> "LEND"
            R.id.rbBorrow -> "BORROW"
            else -> "EXPENSE"
        }
        val paymentMethod = when (binding.rgPayment.checkedRadioButtonId) {
            R.id.rbCash -> "CASH"
            R.id.rbCard -> "CARD"
            else -> "CASH"
        }
        val currency = when (binding.rgCurrency.checkedRadioButtonId) {
            R.id.rbIqd -> "IQD"
            R.id.rbUsd -> "USD"
            else -> "IQD"
        }
        val transaction = TransactionEntity(title = title, amount = amount, type = type, paymentMethod = paymentMethod, currency = currency, date = selectedDate)
        lifecycleScope.launch {
            db.transactionDao().insert(transaction)
            Toast.makeText(this@AddTransactionActivity, "تم الحفظ", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun formatDate(timestamp: Long): String {
        return java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(timestamp))
    }
}
