package com.example.location_tracker

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import org.json.JSONArray
import org.json.JSONObject

class LocationService : Service() {

  private lateinit var fusedClient: FusedLocationProviderClient
  private lateinit var locationCallback: LocationCallback
  private lateinit var activityRecognitionClient: ActivityRecognitionClient
  private val locationProfileReceiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
      fusedClient.removeLocationUpdates(locationCallback)
      startLocationUpdates()
    }
  }
  private val handler = android.os.Handler(Looper.getMainLooper())
  private val heartbeatLogger = object : Runnable {
      override fun run() {
        if (debug) {
          Logger.log("Healthcheck")
        }
        handler.postDelayed(this, 120000L)
      }
  }
  private var activityIntervalInMilliseconds: Long = 2000L
  private var debug: Boolean = false
  companion object {
    @Volatile
    var isRunning: Boolean = false
  }


  override fun onCreate() {
    super.onCreate()
    initializeLogger()
    initializeLocationClients()
  }

  private fun initializeLogger() {
    Logger.init(this)
  }

  private fun startHeartbeatLogger() {
    handler.post(heartbeatLogger)
  }

  private fun initializeLocationClients() {
    fusedClient = LocationServices.getFusedLocationProviderClient(this)
    activityRecognitionClient = ActivityRecognition.getClient(this)
  }

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
      activityIntervalInMilliseconds = intent?.getIntExtra("activity_interval_in_milliseconds", 2000)?.toLong() ?: 2000L
      debug = intent?.getBooleanExtra("debug", false) ?: false
      println(String.format("[LocationTrackerPlugin] DEBUG MODE ENABLED : %s", debug))
      showNotification(intent)
      registerProfileReceiver()
      startActivityUpdates()
      startLocationUpdates()
      startHeartbeatLogger()
      isRunning = true
      return START_STICKY
  }

  private fun showNotification(intent: Intent?) {
    val title = intent?.getStringExtra("notification_title") ?: "Location tracker plugin"
    val content = intent?.getStringExtra("notification_content") ?: "Tracking your location"
    Notification.show(this, title, content)
  }

  private fun registerProfileReceiver() {
    val filter = IntentFilter("com.example.location_tracker.ACTION_UPDATE_LOCATION_PROFILE")
    registerReceiver(locationProfileReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
  }

  private fun startActivityUpdates() {
    val intent = Intent(this, ActivityReceiver::class.java)
    val pendingIntent = PendingIntent.getBroadcast(
      this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
    )
    activityRecognitionClient.requestActivityUpdates(
      activityIntervalInMilliseconds,
      pendingIntent
    )
  }

  private fun getConfiguredLocationRequest(): LocationRequest {
    val prefs = getSharedPreferences("location_prefs", Context.MODE_PRIVATE)
    val activityType = if (debug) {
        DetectedActivity.ON_FOOT
    } else {
        prefs.getInt("last_activity", DetectedActivity.UNKNOWN)
    }
    val profile = ActivityLocationProfile(activityType)
    println(String.format("[LocationTrackerPlugin] CURRENT ACTIVITY IS : %s", profile.label()))
    return LocationRequest.Builder(
        profile.priority(),
        profile.intervalInMilliseconds()
    ).setMinUpdateDistanceMeters(profile.distanceInMeters()).build()
  }

  private fun startLocationUpdates() {
    val request = getConfiguredLocationRequest()
    locationCallback = object : LocationCallback() {
      override fun onLocationResult(result: LocationResult) {
        result.locations.forEach { location ->
          Logger.logCurrentLocation(location.latitude, location.longitude)
          if (debug) {
            Logger.logLocation(location.latitude, location.longitude)
          }
        }
      }
    }
    try {
        fusedClient.requestLocationUpdates(request, locationCallback, Looper.getMainLooper())
    } catch (ex: SecurityException) {
        ex.printStackTrace()
    }
  }

  override fun onDestroy() {
    isRunning = false
    super.onDestroy()
    fusedClient.removeLocationUpdates(locationCallback)
    handler.removeCallbacks(heartbeatLogger)
    unregisterReceiver(locationProfileReceiver)
  }

  override fun onBind(intent: Intent?): IBinder? = null
}
