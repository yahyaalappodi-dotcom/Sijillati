package com.yahya.sijillati

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.yahya.sijillati.database.AppDatabase
import com.yahya.sijillati.databinding.ActivitySettingsBinding
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

        // Load fingerprint state
        binding.switchFingerprint.isChecked = prefs.getBoolean("fingerprint", false)

        // Check if device supports fingerprint
        val biometricHelper = BiometricHelper(this)
        if (!biometricHelper.canAuthenticate()) {
            binding.switchFingerprint.isEnabled = false
            binding.switchFingerprint.text = "البصمة (غير متوفرة على هذا الجهاز)"
        }

        binding.switchFingerprint.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked && !biometricHelper.canAuthenticate()) {
                Toast.makeText(this, "البصمة غير متوفرة على هذا الجهاز", Toast.LENGTH_LONG).show()
                binding.switchFingerprint.isChecked = false
                return@setOnCheckedChangeListener
            }
            prefs.edit().putBoolean("fingerprint", isChecked).apply()
            Toast.makeText(this, if (isChecked) "تم تفعيل البصمة" else "تم تعطيل البصمة", Toast.LENGTH_SHORT).show()
        }

        binding.btnExport.setOnClickListener { Toast.makeText(this, "قريباً", Toast.LENGTH_SHORT).show() }
        binding.btnBackup.setOnClickListener { Toast.makeText(this, "قريباً", Toast.LENGTH_SHORT).show() }
        binding.btnRestore.setOnClickListener { Toast.makeText(this, "قريباً", Toast.LENGTH_SHORT).show() }

        binding.btnDeleteAll.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("حذف جميع البيانات")
                .setMessage("هل أنت متأكد؟ لا يمكن التراجع!")
                .setPositiveButton("نعم، احذف") { _, _ ->
                    lifecycleScope.launch {
                        val all = db.transactionDao().getAll().value
                        all?.forEach { db.transactionDao().delete(it) }
                        Toast.makeText(this@SettingsActivity, "تم حذف جميع البيانات", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("إلغاء", null)
                .show()
        }
    }
}
