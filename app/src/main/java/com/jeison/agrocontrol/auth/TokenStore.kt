package com.jeison.agrocontrol.auth

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class TokenStore(context: Context) {

    private val prefs: SharedPreferences = try {
        createEncryptedPrefs(context)
    } catch (e: Exception) {
        Log.e("TokenStore", "Error al inicializar EncryptedSharedPreferences, reiniciando claves de cifrado", e)
        runCatching { context.deleteSharedPreferences(PREFS_NAME) }
        try {
            createEncryptedPrefs(context)
        } catch (e2: Exception) {
            Log.e("TokenStore", "Fallback a SharedPreferences estándar", e2)
            context.getSharedPreferences("${PREFS_NAME}_fallback", Context.MODE_PRIVATE)
        }
    }

    private fun createEncryptedPrefs(context: Context): SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        return EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    fun saveTokens(access: String, refresh: String) {
        prefs.edit()
            .putString(KEY_ACCESS, access)
            .putString(KEY_REFRESH, refresh)
            .apply()
    }

    fun saveActiveFincaId(fincaId: String) {
        prefs.edit().putString(KEY_FINCA_ID, fincaId).apply()
    }

    fun getActiveFincaId(): String? = prefs.getString(KEY_FINCA_ID, null)

    fun getAccessToken(): String? = prefs.getString(KEY_ACCESS, null)

    fun getRefreshToken(): String? = prefs.getString(KEY_REFRESH, null)

    fun clear() {
        prefs.edit().clear().apply()
    }

    fun isLoggedIn(): Boolean = !getAccessToken().isNullOrBlank()

    companion object {
        private const val PREFS_NAME = "agrocontrol_secure_prefs"
        private const val KEY_ACCESS = "access_token"
        private const val KEY_REFRESH = "refresh_token"
        private const val KEY_FINCA_ID = "active_finca_id"
    }
}
