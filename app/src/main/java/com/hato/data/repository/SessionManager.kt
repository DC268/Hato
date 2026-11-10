package com.hato.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.hato.data.api.ApiClient
import com.hato.domain.model.User

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("hato_session", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun saveToken(token: String) {
        prefs.edit().putString(KEY_TOKEN, token).apply()
        ApiClient.authToken = token
    }

    fun getToken(): String? {
        val token = prefs.getString(KEY_TOKEN, null)
        ApiClient.authToken = token
        return token
    }

    fun saveUser(user: User) {
        prefs.edit().putString(KEY_USER, gson.toJson(user)).apply()
    }

    fun getUser(): User? {
        val json = prefs.getString(KEY_USER, null) ?: return null
        return try {
            gson.fromJson(json, User::class.java)
        } catch (e: Exception) {
            null
        }
    }

    fun isLoggedIn(): Boolean {
        return getToken() != null
    }

    fun clearSession() {
        prefs.edit().clear().apply()
        ApiClient.authToken = null
    }

    companion object {
        private const val KEY_TOKEN = "jwt_token"
        private const val KEY_USER = "current_user"
    }
}
