package com.muchen.virtualcall.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PrefsDataSource @Inject constructor(
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: Context,
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PrefsKeys.FILE_NAME, Context.MODE_PRIVATE)

    fun getString(key: String, default: String? = null): String? =
        prefs.getString(key, default)

    fun getBoolean(key: String, default: Boolean = false): Boolean =
        prefs.getBoolean(key, default)

    fun putString(key: String, value: String?) {
        prefs.edit { putString(key, value) }
    }

    fun putBoolean(key: String, value: Boolean) {
        prefs.edit { putBoolean(key, value) }
    }

    fun remove(key: String) {
        prefs.edit { remove(key) }
    }
}
