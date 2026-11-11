package com.hato.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hato.data.api.ApiClient
import com.hato.domain.model.Animal
import com.hato.domain.model.PolygonGeofence
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

    // Persistencia de Dispositivos Conectados
    fun saveConnectedDevices(devices: List<String>) {
        prefs.edit().putString(KEY_CONNECTED_DEVICES, gson.toJson(devices)).apply()
    }

    fun getConnectedDevices(): List<String> {
        val json = prefs.getString(KEY_CONNECTED_DEVICES, null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<String>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Persistencia de IDs de Trackers Vinculados
    fun saveLinkedTrackerIds(ids: List<String>) {
        prefs.edit().putString(KEY_LINKED_TRACKERS, gson.toJson(ids)).apply()
    }

    fun getLinkedTrackerIds(): List<String> {
        val json = prefs.getString(KEY_LINKED_TRACKERS, null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<String>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Persistencia de Animales del Usuario
    fun saveAnimals(animals: List<Animal>) {
        prefs.edit().putString(KEY_ANIMALS, gson.toJson(animals)).apply()
    }

    fun getAnimals(): List<Animal> {
        val json = prefs.getString(KEY_ANIMALS, null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<Animal>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Persistencia de Potreros del Usuario
    fun saveZones(zones: List<PolygonGeofence>) {
        prefs.edit().putString(KEY_ZONES, gson.toJson(zones)).apply()
    }

    fun getZones(): List<PolygonGeofence> {
        val json = prefs.getString(KEY_ZONES, null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<PolygonGeofence>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Persistencia de Tema Oscuro / Claro
    fun saveDarkMode(isDark: Boolean) {
        prefs.edit().putBoolean(KEY_DARK_MODE, isDark).apply()
    }

    fun isDarkMode(): Boolean {
        return prefs.getBoolean(KEY_DARK_MODE, true)
    }

    fun clearSession() {
        val savedDevices = getConnectedDevices()
        val savedTrackers = getLinkedTrackerIds()
        val savedAnimals = getAnimals()
        val savedZones = getZones()
        val isDark = isDarkMode()

        prefs.edit().clear().apply()
        ApiClient.authToken = null

        // Mantener dispositivos vinculados y preferencias locales
        saveConnectedDevices(savedDevices)
        saveLinkedTrackerIds(savedTrackers)
        saveAnimals(savedAnimals)
        saveZones(savedZones)
        saveDarkMode(isDark)
    }

    companion object {
        private const val KEY_TOKEN = "jwt_token"
        private const val KEY_USER = "current_user"
        private const val KEY_CONNECTED_DEVICES = "connected_devices"
        private const val KEY_LINKED_TRACKERS = "linked_trackers"
        private const val KEY_ANIMALS = "user_animals"
        private const val KEY_ZONES = "user_zones"
        private const val KEY_DARK_MODE = "is_dark_mode"
    }
}
