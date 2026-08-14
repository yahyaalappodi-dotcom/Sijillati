package com.yahya.sijillati

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.yahya.sijillati.database.AppDatabase
import com.yahya.sijillati.databinding.ActivityTransactionLogBinding
import kotlinx.coroutines.launch

class TransactionLogActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTransactionLogBinding
    private lateinit var db: AppDatabase
    private lateinit var adapter: TransactionAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTransactionLogBinding.inflate(layoutInflater)
        setContentView(binding.root)
        db = AppDatabase.getDatabase(this)

        adapter = TransactionAdapter(
            emptyList(),
            onItemClick = { transaction ->
                Toast.makeText(this, transaction.title, Toast.LENGTH_SHORT).show()
            },
            onEditClick = { transaction ->
                val intent = Intent(this, EditTransactionActivity::class.java)
                intent.putExtra("transaction_id", transaction.id)
                startActivity(intent)
            },
            onDeleteClick = { transaction ->
                AlertDialog.Builder(this)
                    .setTitle("حذف المعاملة")
                    .setMessage("هل تريد حذف \"${transaction.title}\"؟")
                    .setPositiveButton("حذف") { _, _ ->
                        lifecycleScope.launch {
                            db.transactionDao().delete(transaction)
                            Toast.makeText(this@TransactionLogActivity, "تم الحذف", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .setNegativeButton("إلغاء", null)
                    .show()
            }
        )
        
        // ✅ الإصلاح الرئيسي: LinearLayoutManager
        binding.recyclerLog.layoutManager = LinearLayoutManager(this)
        binding.recyclerLog.adapter = adapter

        db.transactionDao().getAll().observe(this) { list ->
            adapter.updateList(list)
        }
    }
}
