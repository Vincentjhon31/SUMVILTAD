package com.zynt.sumviltadconnect.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore by preferencesDataStore("settings_prefs")

class SettingsStore(private val context: Context) {
    private val THEME = stringPreferencesKey("theme")
    private val NOTIF = booleanPreferencesKey("notifications_enabled")

    val theme: Flow<String?> = context.settingsDataStore.data.map { it[THEME] }
    val notificationsEnabled: Flow<Boolean> = context.settingsDataStore.data.map { it[NOTIF] ?: true }

    suspend fun setTheme(value: String) { context.settingsDataStore.edit { it[THEME] = value } }
    suspend fun setNotificationsEnabled(value: Boolean) { context.settingsDataStore.edit { it[NOTIF] = value } }
}

