package com.yahya.sijillati

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

class SijillatiApp : Application() {
    override fun onCreate() {
        super.onCreate()
        val prefs = getSharedPreferences("sijillati_prefs", Context.MODE_PRIVATE)
        val dark = prefs.getBoolean("dark_mode", false)
        AppCompatDelegate.setDefaultNightMode(
            if (dark) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )
    }
}
