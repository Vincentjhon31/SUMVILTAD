package com.zynt.sumviltadconnect.utils

import android.content.Context
import android.content.SharedPreferences

object TokenManager {
    private const val PREF_NAME = "auth_prefs"
    private const val TOKEN_KEY = "auth_token"
    private const val USER_NAME_KEY = "user_name"
    private const val USER_EMAIL_KEY = "user_email"
    private const val USER_ID_KEY = "user_id"

    private fun getSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun saveToken(context: Context, token: String) {
        getSharedPreferences(context).edit().putString(TOKEN_KEY, token).apply()
    }

    fun getToken(context: Context): String? {
        return getSharedPreferences(context).getString(TOKEN_KEY, null)
    }

    fun saveUserInfo(context: Context, name: String, email: String, id: Int) {
        val editor = getSharedPreferences(context).edit()
        editor.putString(USER_NAME_KEY, name)
        editor.putString(USER_EMAIL_KEY, email)
        editor.putInt(USER_ID_KEY, id)
        editor.apply()
    }

    fun getUserName(context: Context): String? {
        return getSharedPreferences(context).getString(USER_NAME_KEY, null)
    }

    fun getUserEmail(context: Context): String? {
        return getSharedPreferences(context).getString(USER_EMAIL_KEY, null)
    }

    fun getUserId(context: Context): Int {
        return getSharedPreferences(context).getInt(USER_ID_KEY, -1)
    }

    fun clearAllData(context: Context) {
        getSharedPreferences(context).edit().clear().apply()
    }

    fun isLoggedIn(context: Context): Boolean {
        return getToken(context) != null
    }
}
