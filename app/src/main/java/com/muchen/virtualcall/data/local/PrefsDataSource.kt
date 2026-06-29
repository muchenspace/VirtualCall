package com.muchen.virtualcall.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * SharedPreferences 封装，提供类型安全的读写方法。
 * 保留双通道写入：edit（async apply）用于非关键数据，editSync（sync commit）用于跨进程关键标志。
 */
@Singleton
class PrefsDataSource @Inject constructor(
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: Context,
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PrefsKeys.FILE_NAME, Context.MODE_PRIVATE)

    // --- 读 ---

    fun getString(key: String, default: String? = null): String? =
        prefs.getString(key, default)

    fun getBoolean(key: String, default: Boolean = false): Boolean =
        prefs.getBoolean(key, default)

    // --- 写（异步 apply）---

    fun putString(key: String, value: String?) {
        prefs.edit { putString(key, value) }
    }

    fun putBoolean(key: String, value: Boolean) {
        prefs.edit { putBoolean(key, value) }
    }

    fun remove(key: String) {
        prefs.edit { remove(key) }
    }

    // --- 写（同步 commit，用于跨进程关键标志）---

    /**
     * 同步写入：使用 commit() 确保数据立即持久化到磁盘。
     * 用于跨进程关键标志（如 service_armed），减少主进程/无障碍进程间数据不同步的窗口期。
     */
    fun putBooleanSync(key: String, value: Boolean) {
        prefs.edit().putBoolean(key, value).commit()
    }
}
