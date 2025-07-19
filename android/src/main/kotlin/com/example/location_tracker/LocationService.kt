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
        println("[LocationService] Healthcheck")
        Logger.log("Healthcheck")
        handler.postDelayed(this, 120000L)
      }
  }
  private var activityIntervalInMilliseconds: Long = 2000L

  override fun onCreate() {
    println("[LocationService] start::onCreate()")
    super.onCreate()
    initializeLogger()
    startHeartbeatLogger()
    initializeLocationClients()
    startActivityUpdates()
    startLocationUpdates()
    println("[LocationService] end::onCreate()")
  }

  private fun initializeLogger() {
    println("[LocationService] start::initializeLogger()")
    Logger.init(this)
    println("[LocationService] end::initializeLogger()")
  }

  private fun startHeartbeatLogger() {
    println("[LocationService] start::startHeartbeatLogger()")
    handler.post(heartbeatLogger)
    println("[LocationService] end::startHeartbeatLogger()")
  }

  private fun initializeLocationClients() {
    println("[LocationService] start::initializeLocationClients()")
    fusedClient = LocationServices.getFusedLocationProviderClient(this)
    activityRecognitionClient = ActivityRecognition.getClient(this)
    println("[LocationService] end::initializeLocationClients()")
  }

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
      println("[LocationService] start::onStartCommand()")
      activityIntervalInMilliseconds = intent?.getIntExtra("activity_interval_in_milliseconds", 2000)?.toLong() ?: 2000L
      showNotification(intent)
      registerProfileReceiver()
      println("[LocationService] end::onStartCommand()")
      return START_STICKY
  }

  private fun showNotification(intent: Intent?) {
    println("[LocationService] start::showNotification()")
    val title = intent?.getStringExtra("notification_title") ?: "Location tracker plugin"
    val content = intent?.getStringExtra("notification_content") ?: "Tracking your location"
    Notification.show(this, title, content)
    println("[LocationService] end::showNotification()")
  }

  private fun registerProfileReceiver() {
    println("[LocationService] start::registerProfileReceiver()")
    val filter = IntentFilter("com.example.location_tracker.ACTION_UPDATE_LOCATION_PROFILE")
    registerReceiver(locationProfileReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
    println("[LocationService] end::registerProfileReceiver()")
  }

  private fun startActivityUpdates() {
    println("[LocationService] start::startActivityUpdates()")
    val intent = Intent(this, ActivityReceiver::class.java)
    val pendingIntent = PendingIntent.getBroadcast(
      this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
    )
    activityRecognitionClient.requestActivityUpdates(
      activityIntervalInMilliseconds,
      pendingIntent
    )
    println("[LocationService] end::startActivityUpdates()")
  }

  private fun getConfiguredLocationRequest(): LocationRequest {
    println("[LocationService] start::getConfiguredLocationRequest()")
    val prefs = getSharedPreferences("location_prefs", Context.MODE_PRIVATE)
    val activityType = prefs.getInt("last_activity", DetectedActivity.STILL)
    val profile = ActivityLocationProfile(activityType)
    return LocationRequest.Builder(
        profile.priority(),
        profile.intervalInMilliseconds()
    ).setMinUpdateDistanceMeters(profile.distanceInMeters()).build()
    println("[LocationService] end::getConfiguredLocationRequest()")
  }

  private fun startLocationUpdates() {
    println("[LocationService] start::startLocationUpdates()")
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
    println("[LocationService] end::startLocationUpdates()")
  }

  override fun onDestroy() {
    println("[LocationService] start::onDestroy()")
    super.onDestroy()
    fusedClient.removeLocationUpdates(locationCallback)
    handler.removeCallbacks(heartbeatLogger)
    unregisterReceiver(locationProfileReceiver)
    println("[LocationService] end::onDestroy()")
  }

  override fun onBind(intent: Intent?): IBinder? = null
}
