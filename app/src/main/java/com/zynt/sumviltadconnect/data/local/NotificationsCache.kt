package com.zynt.sumviltadconnect.data.local

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.zynt.sumviltadconnect.data.model.AppNotification

/** Simple SharedPreferences based cache for notifications (offline support). */
class NotificationsCache(private val context: Context) {
    private val prefs = context.getSharedPreferences("notifications_cache", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val key = "notifications_json"

    fun saveNotifications(list: List<AppNotification>) {
        try { prefs.edit().putString(key, gson.toJson(list)).apply() } catch (_: Exception) {}
    }

    fun loadNotifications(): List<AppNotification> {
        val json = prefs.getString(key, null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<AppNotification>>() {}.type
            gson.fromJson<List<AppNotification>>(json, type) ?: emptyList()
        } catch (_: Exception) { emptyList() }
    }

    fun clear() {
        try { prefs.edit().remove(key).apply() } catch (_: Exception) {}
    }
}
