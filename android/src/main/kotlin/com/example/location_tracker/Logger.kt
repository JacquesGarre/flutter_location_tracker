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
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun logLocation(lat: Double, lng: Double) {
        if (!::prefs.isInitialized) {
            throw IllegalStateException("Logger not initialized. Call Logger.init(context) first.")
        }
        println("[Logger] start::logLocation()")
        val activityType = prefs.getInt(KEY_ACTIVITY, DetectedActivity.STILL)
        val profile = ActivityLocationProfile(activityType)
        val activityLabel = profile.label()
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            .format(Date())
        val logEntry = "[$timestamp] lat: $lat, lng: $lng, activity: $activityLabel"
        println("[Logger] New location: $logEntry")
        val logs = prefs.getString(KEY_LOGS, "") ?: ""
        val updatedLogs = if (logs.isEmpty()) logEntry else "$logs\n$logEntry"
        prefs.edit().putString(KEY_LOGS, updatedLogs).apply()
        println("[Logger] end::logLocation()")
    }

    fun logActivity(activityType: Int, confidence: Int) {
        if (!::prefs.isInitialized) {
            throw IllegalStateException("Logger not initialized. Call Logger.init(context) first.")
        }
        println("[Logger] start::logActivity()")
        val profile = ActivityLocationProfile(activityType)
        val activityLabel = profile.label()
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            .format(Date())
        val logEntry = "[$timestamp] Detected activity: $activityLabel (confidence: $confidence%)"
        println("[Logger] New activity: $logEntry")
        val logs = prefs.getString(KEY_LOGS, "") ?: ""
        val updatedLogs = if (logs.isEmpty()) logEntry else "$logs\n$logEntry"
        prefs.edit().putString(KEY_LOGS, updatedLogs).apply()
        println("[Logger] end::logActivity()")
    }

    fun log(message: String) {
        if (!::prefs.isInitialized) {
            throw IllegalStateException("Logger not initialized. Call Logger.init(context) first.")
        }
        println("[Logger] start::log()")
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            .format(Date())
        val logEntry = "[$timestamp] $message"
        println("[Logger] $logEntry")
        val logs = prefs.getString(KEY_LOGS, "") ?: ""
        val updatedLogs = if (logs.isEmpty()) logEntry else "$logs\n$logEntry"
        prefs.edit().putString(KEY_LOGS, updatedLogs).apply()
        println("[Logger] end::log()")
    }

    fun clearLogs() {
        prefs.edit().remove(KEY_LOGS).apply()
    }

    fun getLogs(): String {
        println("[Logger] start::getLogs()")
        val logs = prefs.getString(KEY_LOGS, "") ?: ""
        val lines = logs.lines().filter { it.isNotBlank() }
        val jsonArray = JSONArray()
        for (line in lines) {
            jsonArray.put(line)
        }
        println("[Logger] end::getLogs()")
        return jsonArray.toString()
    }

}