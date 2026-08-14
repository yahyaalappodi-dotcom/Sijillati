package com.yahya.sijillati

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.yahya.sijillati.database.AppDatabase
import com.yahya.sijillati.databinding.ActivitySettingsBinding
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        db = AppDatabase.getDatabase(this)
        val prefs = getSharedPreferences("settings", MODE_PRIVATE)
        binding.switchFingerprint.isChecked = prefs.getBoolean("fingerprint", false)
        binding.switchFingerprint.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("fingerprint", isChecked).apply()
            Toast.makeText(this, if (isChecked) "تم تفعيل البصمة" else "تم تعطيل البصمة", Toast.LENGTH_SHORT).show()
        }
        binding.btnExport.setOnClickListener { Toast.makeText(this, "قريباً", Toast.LENGTH_SHORT).show() }
        binding.btnBackup.setOnClickListener { Toast.makeText(this, "قريباً", Toast.LENGTH_SHORT).show() }
        binding.btnRestore.setOnClickListener { Toast.makeText(this, "قريباً", Toast.LENGTH_SHORT).show() }
        binding.btnDeleteAll.setOnClickListener {
            AlertDialog.Builder(this).setTitle("حذف جميع البيانات").setMessage("هل أنت متأكد؟")
                .setPositiveButton("نعم") { _, _ ->
                    GlobalScope.launch { db.transactionDao().getAll().value?.forEach { db.transactionDao().delete(it) } }
                    Toast.makeText(this, "تم الحذف", Toast.LENGTH_SHORT).show()
                }.setNegativeButton("إلغاء", null).show()
        }
    }
}
