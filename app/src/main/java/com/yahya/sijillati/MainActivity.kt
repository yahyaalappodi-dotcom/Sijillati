package com.yahya.sijillati

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.yahya.sijillati.database.AppDatabase
import com.yahya.sijillati.databinding.ActivityMainBinding
import kotlinx.coroutines.launch
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var db: AppDatabase
    private lateinit var adapter: TransactionAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("settings", MODE_PRIVATE)
        val fingerprintEnabled = prefs.getBoolean("fingerprint", false)

        if (fingerprintEnabled) {
            val biometricHelper = BiometricHelper(this)
            if (biometricHelper.canAuthenticate()) {
                setContentView(R.layout.activity_splash)
                biometricHelper.authenticate(
                    onSuccess = { setupMainUI() },
                    onError = { error ->
                        Toast.makeText(this, error, Toast.LENGTH_LONG).show()
                        prefs.edit().putBoolean("fingerprint", false).apply()
                        setupMainUI()
                    }
                )
                return
            } else {
                Toast.makeText(this, "البصمة غير متوفرة على هذا الجهاز", Toast.LENGTH_SHORT).show()
                prefs.edit().putBoolean("fingerprint", false).apply()
            }
        }
        setupMainUI()
    }

    private fun setupMainUI() {
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        db = AppDatabase.getDatabase(this)

        NotificationHelper(this).createChannel()
        WorkScheduler.scheduleDailyReminder(this)

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
                lifecycleScope.launch {
                    db.transactionDao().delete(transaction)
                    Toast.makeText(this@MainActivity, "تم الحذف", Toast.LENGTH_SHORT).show()
                }
            }
        )
        
        // ✅ إصلاح: LinearLayoutManager
        binding.recyclerTransactions.layoutManager = LinearLayoutManager(this)
        binding.recyclerTransactions.adapter = adapter

        binding.btnAdd.setOnClickListener {
            startActivity(Intent(this, AddTransactionActivity::class.java))
        }
        binding.btnReports.setOnClickListener {
            startActivity(Intent(this, ReportsActivity::class.java))
        }
        binding.btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        binding.btnLog.setOnClickListener {
            startActivity(Intent(this, TransactionLogActivity::class.java))
        }

        observeData()
    }

    private fun observeData() {
        db.transactionDao().getTotalIncome("IQD").observe(this) { inc ->
            db.transactionDao().getTotalExpense("IQD").observe(this) { exp ->
                val wallet = (inc ?: 0.0) - (exp ?: 0.0)
                binding.tvWalletIqd.text = "%,.0f د.ع".format(Locale.US, wallet)
            }
        }
        db.transactionDao().getTotalIncome("USD").observe(this) { inc ->
            db.transactionDao().getTotalExpense("USD").observe(this) { exp ->
                val wallet = (inc ?: 0.0) - (exp ?: 0.0)
                binding.tvWalletUsd.text = "%,.0f $".format(Locale.US, wallet)
            }
        }
        db.transactionDao().getTotalLent("IQD").observe(this) { iqd ->
            db.transactionDao().getTotalLent("USD").observe(this) { usd ->
                binding.tvLent.text = "إقراض: %,.0f".format(Locale.US, (iqd ?: 0.0) + (usd ?: 0.0))
            }
        }
        db.transactionDao().getTotalBorrowed("IQD").observe(this) { iqd ->
            db.transactionDao().getTotalBorrowed("USD").observe(this) { usd ->
                binding.tvBorrowed.text = "اقتراض: %,.0f".format(Locale.US, (iqd ?: 0.0) + (usd ?: 0.0))
            }
        }
        db.transactionDao().getAll().observe(this) { list ->
            adapter.updateList(list.take(10))
        }
    }

    override fun onResume() {
        super.onResume()
        db.transactionDao().getAll().observe(this) { list ->
            adapter.updateList(list.take(10))
        }
    }
}
package com.yahya.sijillati

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.yahya.sijillati.database.AppDatabase
import com.yahya.sijillati.database.TransactionEntity
import com.yahya.sijillati.databinding.ActivityMainBinding
import kotlinx.coroutines.launch
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var db: AppDatabase
    private lateinit var adapter: TransactionAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("settings", MODE_PRIVATE)
        val fingerprintEnabled = prefs.getBoolean("fingerprint", false)

        if (fingerprintEnabled) {
            val biometricHelper = BiometricHelper(this)
            if (biometricHelper.canAuthenticate()) {
                setContentView(R.layout.activity_splash)
                biometricHelper.authenticate(
                    onSuccess = { setupMainUI() },
                    onError = { error ->
                        Toast.makeText(this, error, Toast.LENGTH_LONG).show()
                        prefs.edit().putBoolean("fingerprint", false).apply()
                        setupMainUI()
                    }
                )
                return
            } else {
                Toast.makeText(this, "البصمة غير متوفرة، تم تعطيلها", Toast.LENGTH_SHORT).show()
                prefs.edit().putBoolean("fingerprint", false).apply()
            }
        }
        setupMainUI()
    }

    private fun setupMainUI() {
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        db = AppDatabase.getDatabase(this)

        NotificationHelper(this).createChannel()
        WorkScheduler.scheduleDailyReminder(this)

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
                lifecycleScope.launch {
                    db.transactionDao().delete(transaction)
                    Toast.makeText(this@MainActivity, "تم الحذف", Toast.LENGTH_SHORT).show()
                }
            }
        )
        binding.recyclerTransactions.adapter = adapter

        binding.btnAdd.setOnClickListener {
            startActivity(Intent(this, AddTransactionActivity::class.java))
        }
        binding.btnReports.setOnClickListener {
            startActivity(Intent(this, ReportsActivity::class.java))
        }
        binding.btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        binding.btnLog.setOnClickListener {
            startActivity(Intent(this, TransactionLogActivity::class.java))
        }

        observeData()
    }

    private fun observeData() {
        db.transactionDao().getTotalIncome("IQD").observe(this) { inc ->
            db.transactionDao().getTotalExpense("IQD").observe(this) { exp ->
                val wallet = (inc ?: 0.0) - (exp ?: 0.0)
                binding.tvWalletIqd.text = "%,.0f د.ع".format(Locale.US, wallet)
            }
        }
        db.transactionDao().getTotalIncome("USD").observe(this) { inc ->
            db.transactionDao().getTotalExpense("USD").observe(this) { exp ->
                val wallet = (inc ?: 0.0) - (exp ?: 0.0)
                binding.tvWalletUsd.text = "%,.0f $".format(Locale.US, wallet)
            }
        }
        db.transactionDao().getTotalLent("IQD").observe(this) { iqd ->
            db.transactionDao().getTotalLent("USD").observe(this) { usd ->
                binding.tvLent.text = "إقراض: %,.0f".format(Locale.US, (iqd ?: 0.0) + (usd ?: 0.0))
            }
        }
        db.transactionDao().getTotalBorrowed("IQD").observe(this) { iqd ->
            db.transactionDao().getTotalBorrowed("USD").observe(this) { usd ->
                binding.tvBorrowed.text = "اقتراض: %,.0f".format(Locale.US, (iqd ?: 0.0) + (usd ?: 0.0))
            }
        }
        db.transactionDao().getAll().observe(this) { list ->
            adapter.updateList(list.take(10))
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh data when coming back
        db.transactionDao().getAll().observe(this) { list ->
            adapter.updateList(list.take(10))
        }
    }
}
