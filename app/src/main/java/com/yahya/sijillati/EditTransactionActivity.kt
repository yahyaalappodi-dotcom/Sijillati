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

class EditTransactionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddTransactionBinding
    private lateinit var db: AppDatabase
    private var transactionId: Int = 0
    private var selectedDate: Long = System.currentTimeMillis()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddTransactionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        db = AppDatabase.getDatabase(this)

        transactionId = intent.getIntExtra("transaction_id", 0)
        if (transactionId == 0) {
            Toast.makeText(this, "خطأ في تحميل المعاملة", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        binding.btnSave.text = "حفظ التعديلات"

        lifecycleScope.launch {
            val transaction = db.transactionDao().getById(transactionId)
            if (transaction != null) {
                loadTransaction(transaction)
            } else {
                Toast.makeText(this@EditTransactionActivity, "المعاملة غير موجودة", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        binding.btnPickDate.setOnClickListener { showDatePicker() }
        binding.btnSave.setOnClickListener { updateTransaction() }
    }

    private fun loadTransaction(transaction: TransactionEntity) {
        binding.etTitle.setText(transaction.title)
        binding.etAmount.setText(transaction.amount.toString())
        selectedDate = transaction.date
        binding.tvDate.text = formatDate(selectedDate)

        when (transaction.type) {
            "INCOME" -> binding.rgType.check(R.id.rbIncome)
            "EXPENSE" -> binding.rgType.check(R.id.rbExpense)
            "LEND" -> binding.rgType.check(R.id.rbLend)
            "BORROW" -> binding.rgType.check(R.id.rbBorrow)
        }
        when (transaction.paymentMethod) {
            "CASH" -> binding.rgPayment.check(R.id.rbCash)
            "CARD" -> binding.rgPayment.check(R.id.rbCard)
        }
        when (transaction.currency) {
            "IQD" -> binding.rgCurrency.check(R.id.rbIqd)
            "USD" -> binding.rgCurrency.check(R.id.rbUsd)
        }
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

    private fun updateTransaction() {
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
        val transaction = TransactionEntity(
            id = transactionId,
            title = title,
            amount = amount,
            type = type,
            paymentMethod = paymentMethod,
            currency = currency,
            date = selectedDate
        )
        lifecycleScope.launch {
            db.transactionDao().update(transaction)
            Toast.makeText(this@EditTransactionActivity, "تم التعديل بنجاح", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun formatDate(timestamp: Long): String {
        return java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(timestamp))
    }
}
