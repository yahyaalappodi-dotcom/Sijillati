package com.yahya.sijillati

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*

class AddTransactionActivity : AppCompatActivity() {
    private lateinit var etTitle: EditText
    private lateinit var etAmount: EditText
    private lateinit var rgType: RadioGroup
    private lateinit var rgMethod: RadioGroup
    private lateinit var spinnerCurrency: Spinner
    private lateinit var tvDate: TextView
    private lateinit var manager: TransactionManager
    private var selectedDate = Calendar.getInstance()
    private var editingId = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_transaction)
        manager = TransactionManager(this)

        etTitle = findViewById(R.id.etTitle)
        etAmount = findViewById(R.id.etAmount)
        rgType = findViewById(R.id.rgType)
        rgMethod = findViewById(R.id.rgMethod)
        spinnerCurrency = findViewById(R.id.spinnerCurrency)
        tvDate = findViewById(R.id.tvDate)

        spinnerCurrency.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, arrayOf("د.ع", "$"))

        editingId = intent.getIntExtra("EXTRA_ID", -1)
        if (editingId != -1) load(editingId) else {
            rgType.check(R.id.rbExpense); rgMethod.check(R.id.rbCash); updateDate()
        }

        findViewById<Button>(R.id.btnDate).setOnClickListener { pickDate() }
        findViewById<Button>(R.id.btnSave).setOnClickListener { save() }
    }

    private fun load(id: Int) {
        val t = manager.getAllTransactions(true).find { it.id == id } ?: return
        etTitle.setText(t.title); etAmount.setText(t.amount.toString())
        rgType.check(when (t.type) {
            "دخل" -> R.id.rbIncome
            "مصروف" -> R.id.rbExpense
            "إقراض" -> R.id.rbLent
            else -> R.id.rbBorrowed
        })
        rgMethod.check(if (t.paymentMethod == "ماستر") R.id.rbMaster else R.id.rbCash)
        spinnerCurrency.setSelection(if (t.currency == "$") 1 else 0)
        try { SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(t.date)?.let { selectedDate.time = it } } catch (_: Exception) {}
        updateDate()
    }

    private fun pickDate() {
        DatePickerDialog(this, { _, y, m, d -> selectedDate.set(y, m, d); updateDate() },
            selectedDate.get(Calendar.YEAR), selectedDate.get(Calendar.MONTH), selectedDate.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun updateDate() {
        tvDate.text = "التاريخ: " + SimpleDateFormat("yyyy-MM-dd", Locale.US).format(selectedDate.time)
    }

    private fun save() {
        val title = etTitle.text.toString().trim()
        val amount = etAmount.text.toString().trim().toDoubleOrNull()
        if (title.isEmpty()) { etTitle.error = "أدخل عنوان المعاملة"; return }
        if (amount == null || amount <= 0) { etAmount.error = "أدخل مبلغاً صحيحاً"; return }

        val type = when (rgType.checkedRadioButtonId) {
            R.id.rbIncome -> "دخل"
            R.id.rbExpense -> "مصروف"
            R.id.rbLent -> "إقراض"
            else -> "اقتراض"
        }
        val method = if (rgMethod.checkedRadioButtonId == R.id.rbMaster) "ماستر" else "كاش"
        val currency = spinnerCurrency.selectedItem.toString()
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(selectedDate.time)

        val old = if (editingId != -1) manager.getAllTransactions(true).find { it.id == editingId } else null
        val t = Transaction(
            editingId.takeIf { it != -1 } ?: System.currentTimeMillis().toInt(),
            title, amount, type, method, currency, date, old?.isArchived ?: false
        )
        if (old == null) manager.addTransaction(t) else manager.updateTransaction(t)
        Toast.makeText(this, if (old == null) "تم الحفظ بنجاح" else "تم تعديل المعاملة بنجاح", Toast.LENGTH_SHORT).show()
        finish()
    }
}
