package com.yahya.sijillati

import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat

object BiometricHelper {
    fun isAvailable(activity: AppCompatActivity): Boolean {
        val manager = BiometricManager.from(activity)
        return manager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK) ==
                BiometricManager.BIOMETRIC_SUCCESS
    }

    fun authenticate(activity: AppCompatActivity, onSuccess: () -> Unit, onFail: () -> Unit) {
        val executor = ContextCompat.getMainExecutor(activity)
        val prompt = BiometricPrompt(activity, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                onSuccess()
            }
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                onFail()
            }
            override fun onAuthenticationFailed() {
                onFail()
            }
        })
        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle("فتح سجلاتي")
            .setSubtitle("استخدم بصمتك لفتح التطبيق")
            .setNegativeButtonText("إلغاء")
            .build()
        prompt.authenticate(info)
    }
}
