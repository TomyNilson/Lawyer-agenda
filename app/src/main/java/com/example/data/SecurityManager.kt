package com.example.data

import android.content.Context
import android.content.SharedPreferences
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SecurityManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("lawyer_security_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_PASSWORD_HASH = "password_hash"
        private const val KEY_IS_LOCKED = "is_locked"
        private const val KEY_LAST_BACKUP = "last_backup_date"
        private const val KEY_REMINDER_ENABLED = "reminder_enabled"
        private const val KEY_REMINDER_DAYS_BEFORE = "reminder_days_before"
    }

    fun isPasswordSet(): Boolean {
        return !prefs.getString(KEY_PASSWORD_HASH, null).isNullOrEmpty()
    }

    fun setPassword(password: String) {
        val hash = hashString(password)
        prefs.edit().putString(KEY_PASSWORD_HASH, hash).putBoolean(KEY_IS_LOCKED, true).apply()
    }

    fun verifyPassword(input: String): Boolean {
        val storedHash = prefs.getString(KEY_PASSWORD_HASH, "") ?: ""
        return hashString(input) == storedHash
    }

    fun removePassword() {
        prefs.edit().remove(KEY_PASSWORD_HASH).putBoolean(KEY_IS_LOCKED, false).apply()
    }

    fun isAppLocked(): Boolean {
        if (!isPasswordSet()) return false
        return prefs.getBoolean(KEY_IS_LOCKED, true)
    }

    fun setAppLocked(locked: Boolean) {
        prefs.edit().putBoolean(KEY_IS_LOCKED, locked).apply()
    }

    fun getLastBackupDate(): String {
        return prefs.getString(KEY_LAST_BACKUP, "لم يتم إجراء نسخ احتياطي بعد") ?: "لم يتم"
    }

    fun recordBackupDate() {
        val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
        prefs.edit().putString(KEY_LAST_BACKUP, dateStr).apply()
    }

    fun isReminderEnabled(): Boolean {
        return prefs.getBoolean(KEY_REMINDER_ENABLED, true)
    }

    fun setReminderEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_REMINDER_ENABLED, enabled).apply()
    }

    fun getReminderDaysBefore(): Int {
        return prefs.getInt(KEY_REMINDER_DAYS_BEFORE, 2) // Default 2 days before
    }

    fun setReminderDaysBefore(days: Int) {
        prefs.edit().putInt(KEY_REMINDER_DAYS_BEFORE, days).apply()
    }

    private fun hashString(input: String): String {
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            val hash = digest.digest(input.toByteArray())
            hash.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            input // fallback
        }
    }
}
