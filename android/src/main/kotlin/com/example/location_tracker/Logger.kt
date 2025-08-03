package com.example.location_tracker

import android.content.Context
import android.content.SharedPreferences
import com.google.android.gms.location.DetectedActivity
import java.text.SimpleDateFormat
import java.util.*
import org.json.JSONObject
import org.json.JSONArray

object Logger {

    private const val PREF_NAME = "location_prefs"
    private const val KEY_LOGS = "location_logs"
    private const val KEY_ACTIVITY = "last_activity"
    private const val KEY_CURRENT_LOCATION = "current_location"

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun logLocation(lat: Double, lng: Double) {
        if (!::prefs.isInitialized) {
            throw IllegalStateException("Logger not initialized. Call Logger.init(context) first.")
        }
        val activityType = prefs.getInt(KEY_ACTIVITY, DetectedActivity.STILL)
        val profile = ActivityLocationProfile(activityType)
        val activityLabel = profile.label()
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            .format(Date())
        val logEntry = "[$timestamp] lat: $lat, lng: $lng, activity: $activityLabel"
        val logs = prefs.getString(KEY_LOGS, "") ?: ""
        val updatedLogs = if (logs.isEmpty()) logEntry else "$logs\n$logEntry"
        prefs.edit().putString(KEY_LOGS, updatedLogs).apply()
    }

    fun logCurrentLocation(lat: Double, lng: Double) {
        if (!::prefs.isInitialized) {
            throw IllegalStateException("Logger not initialized. Call Logger.init(context) first.")
        }
        val logJson = JSONObject().apply {
            put("lat", lat)
            put("lng", lng)
        }
        val jsonLogsStr = prefs.getString(KEY_CURRENT_LOCATION, "[]")
        val jsonArray = JSONArray(jsonLogsStr)
        jsonArray.put(logJson)
        prefs.edit().putString(KEY_CURRENT_LOCATION, jsonArray.toString()).apply()
        println("[LocationTrackerPlugin] CURRENT LOCATION: $logJson")
    }

    fun getCurrentLocation(): Pair<Double, Double>? {
        if (!::prefs.isInitialized) {
            throw IllegalStateException("Logger not initialized. Call Logger.init(context) first.")
        }
        val jsonLogsStr = prefs.getString(KEY_CURRENT_LOCATION, "[]")
        val jsonArray = JSONArray(jsonLogsStr)
        if (jsonArray.length() == 0) return null
        val latestEntry = jsonArray.getJSONObject(jsonArray.length() - 1)
        val lat = latestEntry.getDouble("lat")
        val lng = latestEntry.getDouble("lng")
        return Pair(lat, lng)
    }

    fun logActivity(activityType: Int, confidence: Int) {
        if (!::prefs.isInitialized) {
            throw IllegalStateException("Logger not initialized. Call Logger.init(context) first.")
        }
        val profile = ActivityLocationProfile(activityType)
        val activityLabel = profile.label()
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            .format(Date())
        val logEntry = "[$timestamp] Detected activity: $activityLabel (confidence: $confidence%)"
        println("[LocationTrackerPlugin] NEW ACTIVITY RECOGNIZED: $logEntry")
        val logs = prefs.getString(KEY_LOGS, "") ?: ""
        val updatedLogs = if (logs.isEmpty()) logEntry else "$logs\n$logEntry"
        prefs.edit().putString(KEY_LOGS, updatedLogs).apply()
    }

    fun log(message: String) {
        if (!::prefs.isInitialized) {
            throw IllegalStateException("Logger not initialized. Call Logger.init(context) first.")
        }
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            .format(Date())
        val logEntry = "[$timestamp] $message"
        println("[LocationTrackerPlugin] $logEntry")
        val logs = prefs.getString(KEY_LOGS, "") ?: ""
        val updatedLogs = if (logs.isEmpty()) logEntry else "$logs\n$logEntry"
        prefs.edit().putString(KEY_LOGS, updatedLogs).apply()
    }

    fun clearLogs() {
        prefs.edit().remove(KEY_LOGS).apply()
    }

    fun getLogs(): String {
        val logs = prefs.getString(KEY_LOGS, "") ?: ""
        val lines = logs.lines().filter { it.isNotBlank() }
        val jsonArray = JSONArray()
        for (line in lines) {
            jsonArray.put(line)
        }
        return jsonArray.toString()
    }

}