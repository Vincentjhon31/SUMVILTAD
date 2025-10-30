package com.zynt.sumviltadconnect.data.local

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.zynt.sumviltadconnect.data.model.IrrigationSchedule

/** Simple SharedPreferences based cache for irrigation schedules (offline support). */
class IrrigationCache(private val context: Context) {
    private val prefs = context.getSharedPreferences("irrigation_cache", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val scheduleKey = "irrigation_schedules_json"
    private val locationKey = "user_location"

    fun saveSchedules(list: List<IrrigationSchedule>, userLocation: String?) {
        try {
            prefs.edit()
                .putString(scheduleKey, gson.toJson(list))
                .putString(locationKey, userLocation)
                .apply()
        } catch (_: Exception) {}
    }

    fun loadSchedules(): List<IrrigationSchedule> {
        val json = prefs.getString(scheduleKey, null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<IrrigationSchedule>>() {}.type
            gson.fromJson<List<IrrigationSchedule>>(json, type) ?: emptyList()
        } catch (_: Exception) { emptyList() }
    }

    fun getUserLocation(): String? {
        return prefs.getString(locationKey, null)
    }

    fun clear() {
        try {
            prefs.edit()
                .remove(scheduleKey)
                .remove(locationKey)
                .apply()
        } catch (_: Exception) {}
    }
}
