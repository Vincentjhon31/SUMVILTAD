package com.zynt.sumviltadconnect.data.local

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.zynt.sumviltadconnect.data.model.Task

/** Simple SharedPreferences based cache for tasks (offline support). */
class TaskCache(private val context: Context) {
    private val prefs = context.getSharedPreferences("tasks_cache", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val key = "tasks_json"

    fun saveTasks(list: List<Task>) {
        try { prefs.edit().putString(key, gson.toJson(list)).apply() } catch (_: Exception) {}
    }

    fun loadTasks(): List<Task> {
        val json = prefs.getString(key, null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<Task>>() {}.type
            gson.fromJson<List<Task>>(json, type) ?: emptyList()
        } catch (_: Exception) { emptyList() }
    }
}

