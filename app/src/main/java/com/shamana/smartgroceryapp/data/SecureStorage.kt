package com.shamana.smartgroceryapp.data

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

object SecureStorage {

    private const val PREFS_NAME = "secure_data"

    fun getPrefs(context: Context): EncryptedSharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        return EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        ) as EncryptedSharedPreferences
    }

    fun saveLastScan(context: Context, rawText: String) {
        val prefs = getPrefs(context)
        prefs.edit().putString("last_scan", rawText).apply()
    }

    fun getLastScan(context: Context): String? {
        val prefs = getPrefs(context)
        return prefs.getString("last_scan", null)
    }
}
