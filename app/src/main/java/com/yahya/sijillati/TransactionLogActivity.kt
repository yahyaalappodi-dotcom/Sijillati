package com.yahya.sijillati

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.yahya.sijillati.database.AppDatabase
import com.yahya.sijillati.databinding.ActivityTransactionLogBinding

class TransactionLogActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTransactionLogBinding
    private lateinit var db: AppDatabase
    private lateinit var adapter: TransactionAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTransactionLogBinding.inflate(layoutInflater)
        setContentView(binding.root)
        db = AppDatabase.getDatabase(this)
        adapter = TransactionAdapter(emptyList()) {}
        binding.recyclerLog.adapter = adapter
        db.transactionDao().getAll().observe(this) { list -> adapter.updateList(list) }
    }
}
