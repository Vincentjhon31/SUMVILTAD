package com.zynt.sumviltadconnect.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.zynt.sumviltadconnect.data.local.SettingsStore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(app: Application): AndroidViewModel(app) {
    private val store = SettingsStore(app.applicationContext)

    val theme: StateFlow<String?> = store.theme.stateIn(viewModelScope, SharingStarted.Eagerly, null)
    val notificationsEnabled: StateFlow<Boolean> = store.notificationsEnabled.stateIn(viewModelScope, SharingStarted.Eagerly, true)

    fun setTheme(value: String) { viewModelScope.launch { store.setTheme(value) } }
    fun setNotificationsEnabled(enabled: Boolean) { viewModelScope.launch { store.setNotificationsEnabled(enabled) } }
}

