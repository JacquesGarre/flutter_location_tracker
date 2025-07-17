package com.example.location_tracker

import android.content.Context
import android.content.SharedPreferences
import com.google.android.gms.location.DetectedActivity
import java.text.SimpleDateFormat
import java.util.*
import org.json.JSONArray

object Logger {

    private const val PREF_NAME = "location_prefs"
    private const val KEY_LOGS = "location_logs"
    private const val KEY_ACTIVITY = "last_activity"

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        println("[Logger] Initializing...")
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        println("[Logger] Initialized.")
    }

    fun logLocation(lat: Double, lng: Double) {
        println("[Logger] Logging location...")
        if (!::prefs.isInitialized) {
            throw IllegalStateException("Logger not initialized. Call Logger.init(context) first.")
        }
        val activityType = prefs.getInt(KEY_ACTIVITY, DetectedActivity.STILL)
        val activity = when (activityType) {
            DetectedActivity.IN_VEHICLE -> "In vehicle"
            DetectedActivity.ON_BICYCLE -> "On bicycle"
            DetectedActivity.RUNNING -> "Running"
            DetectedActivity.WALKING -> "Walking"
            DetectedActivity.STILL -> "Still"
            else -> "Unknown"
        }
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            .format(Date())
        val logEntry = "[$timestamp] lat: $lat, lng: $lng, activity: $activity"
        val logs = prefs.getString(KEY_LOGS, "") ?: ""
        val updatedLogs = if (logs.isEmpty()) logEntry else "$logs\n$logEntry"
        prefs.edit().putString(KEY_LOGS, updatedLogs).apply()
        println("[Logger] Location logged.")
    }

    fun logActivity(activityType: Int, confidence: Int) {
        println("[Logger] Logging activity...")
        if (!::prefs.isInitialized) {
            throw IllegalStateException("Logger not initialized. Call Logger.init(context) first.")
        }
        val activity = when (activityType) {
            DetectedActivity.IN_VEHICLE -> "In vehicle"
            DetectedActivity.ON_BICYCLE -> "On bicycle"
            DetectedActivity.RUNNING -> "Running"
            DetectedActivity.WALKING -> "Walking"
            DetectedActivity.STILL -> "Still"
            else -> "Unknown"
        }
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            .format(Date())
        val logEntry = "[$timestamp] Detected activity: $activity (confidence: $confidence%)"
        val logs = prefs.getString(KEY_LOGS, "") ?: ""
        val updatedLogs = if (logs.isEmpty()) logEntry else "$logs\n$logEntry"
        prefs.edit().putString(KEY_LOGS, updatedLogs).apply()
        println("[Logger] Activity logged.")
    }

    fun log(message: String) {
        println("[Logger] Logging message...")
        if (!::prefs.isInitialized) {
            throw IllegalStateException("Logger not initialized. Call Logger.init(context) first.")
        }
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            .format(Date())
        val logEntry = "[$timestamp] $message"
        val logs = prefs.getString(KEY_LOGS, "") ?: ""
        val updatedLogs = if (logs.isEmpty()) logEntry else "$logs\n$logEntry"
        prefs.edit().putString(KEY_LOGS, updatedLogs).apply()
        println("[Logger] Message logged.")
    }

    fun clearLogs() {
        prefs.edit().remove(KEY_LOGS).apply()
    }

    fun getLogs(): String {
        println("[Logger] Getting logs...")
        val logs = prefs.getString(KEY_LOGS, "") ?: ""
        val lines = logs.lines().filter { it.isNotBlank() }
        val jsonArray = JSONArray()
        for (line in lines) {
            jsonArray.put(line)
        }
        return jsonArray.toString()
    }

}