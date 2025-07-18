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
          handler.postDelayed(this, 120000L) // every 120 seconds // TODO: Make it as a parameter
      }
  }

  override fun onCreate() {
    super.onCreate()
    initializeLogger()
    startHeartbeatLogger()
    initializeLocationClients()
    Notification.show(this)
    startActivityUpdates()
    startLocationUpdates()
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
      registerProfileReceiver()
      return START_STICKY
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
      2000, // every 2 seconds // TODO: Make it as a parameter
      pendingIntent
    )
  }

  private fun getConfiguredLocationRequest(): LocationRequest {
    val prefs = getSharedPreferences("location_prefs", Context.MODE_PRIVATE)
    val activityType = prefs.getInt("last_activity", DetectedActivity.STILL)
    val profile = ActivityLocationProfile(activityType)
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
                Logger.logLocation(location.latitude, location.longitude)
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
    super.onDestroy()
    fusedClient.removeLocationUpdates(locationCallback)
    handler.removeCallbacks(heartbeatLogger)
  }

  override fun onBind(intent: Intent?): IBinder? = null
}
