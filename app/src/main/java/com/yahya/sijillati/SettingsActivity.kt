package com.yahya.sijillati

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("sijillati_prefs", MODE_PRIVATE)

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
        }

        container.addView(TextView(this).apply {
            text = "الإعدادات"
            textSize = 22f
            setPadding(0, 0, 0, 32)
        })

        // الوضع الليلي
        val darkRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; setPadding(0, 20, 0, 20) }
        val darkLabel = TextView(this).apply { text = "الوضع الليلي"; textSize = 16f; layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f) }
        val darkSwitch = Switch(this).apply { isChecked = prefs.getBoolean("dark_mode", false) }
        darkSwitch.setOnCheckedChangeListener { _, checked ->
            prefs.edit().putBoolean("dark_mode", checked).apply()
            recreateApp()
        }
        darkRow.addView(darkLabel); darkRow.addView(darkSwitch)
        container.addView(darkRow)

        // البصمة
        val bioRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; setPadding(0, 20, 0, 20) }
        val bioLabel = TextView(this).apply { text = "قفل بالبصمة"; textSize = 16f; layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f) }
        val bioSwitch = Switch(this).apply { isChecked = prefs.getBoolean("biometric_lock", false) }
        bioSwitch.setOnCheckedChangeListener { _, checked ->
            if (checked && !BiometricHelper.isAvailable(this)) {
                bioSwitch.isChecked = false
            } else {
                prefs.edit().putBoolean("biometric_lock", checked).apply()
            }
        }
        bioRow.addView(bioLabel); bioRow.addView(bioSwitch)
        container.addView(bioRow)

        // التذكير اليومي
        val notifRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; setPadding(0, 20, 0, 20) }
        val notifLabel = TextView(this).apply { text = "تذكير يومي (9 مساءً)"; textSize = 16f; layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f) }
        val notifSwitch = Switch(this).apply { isChecked = prefs.getBoolean("daily_notif", false) }
        notifSwitch.setOnCheckedChangeListener { _, checked ->
            prefs.edit().putBoolean("daily_notif", checked).apply()
            if (checked) {
                if (Build.VERSION.SDK_INT >= 33 &&
                    ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1)
                }
                NotificationHelper.createChannel(this)
                NotificationHelper.scheduleDaily(this)
            } else {
                NotificationHelper.cancel(this)
            }
        }
        notifRow.addView(notifLabel); notifRow.addView(notifSwitch)
        container.addView(notifRow)

        val scroll = NestedScrollView(this)
        scroll.addView(container)
        setContentView(scroll)
    }

    private fun recreateApp() {
        val prefs = getSharedPreferences("sijillati_prefs", MODE_PRIVATE)
        val dark = prefs.getBoolean("dark_mode", false)
        androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(
            if (dark) androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES else androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
        )
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }
}
