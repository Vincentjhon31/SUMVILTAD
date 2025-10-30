package com.zynt.sumviltadconnect.data.local

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.zynt.sumviltadconnect.data.model.Event

/** Simple SharedPreferences based cache for events (offline support). */
class EventsCache(private val context: Context) {
    private val prefs = context.getSharedPreferences("events_cache", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val key = "events_json"

    fun saveEvents(list: List<Event>) {
        try { prefs.edit().putString(key, gson.toJson(list)).apply() } catch (_: Exception) {}
    }

    fun loadEvents(): List<Event> {
        val json = prefs.getString(key, null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<Event>>() {}.type
            gson.fromJson<List<Event>>(json, type) ?: emptyList()
        } catch (_: Exception) { emptyList() }
    }

    fun clear() {
        try { prefs.edit().remove(key).apply() } catch (_: Exception) {}
    }
}
