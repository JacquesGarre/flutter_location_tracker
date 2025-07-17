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
      println("[LocationService] Received update location profile action...")
      println("[LocationService] Removing location updates...")
      fusedClient.removeLocationUpdates(locationCallback)
      println("[LocationService] Location updates removed.")
      startLocationUpdates()
    }
  }
  private val handler = android.os.Handler(Looper.getMainLooper())
  private val heartbeatLogger = object : Runnable {
      override fun run() {
          println("[LocationService] Logging heartbeat...")
          Logger.log("Healthcheck: LocationService is up and running in the background")
          handler.postDelayed(this, 30_000L) // 30 seconds
          println("[LocationService] Heartbeat logged.")
      }
  }

  override fun onCreate() {
    println("[LocationService] Starting location service...")
    super.onCreate()
    Logger.init(this)
    handler.post(heartbeatLogger)
    fusedClient = LocationServices.getFusedLocationProviderClient(this)
    activityRecognitionClient = ActivityRecognition.getClient(this)
    showNotification()
    startActivityUpdates()
    startLocationUpdates()
    println("[LocationService] Location service started.")
  }

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
      println("[LocationService] Command starting...")
      val filter = IntentFilter("com.example.location_tracker.ACTION_UPDATE_LOCATION_PROFILE")
      registerReceiver(
        locationProfileReceiver,
        filter,
        Context.RECEIVER_NOT_EXPORTED
      )
      println("[LocationService] Command started.")
      return START_STICKY
  }

  private fun startActivityUpdates() {
    println("[LocationService] Starting activity updates...")
    val intent = Intent(this, ActivityReceiver::class.java)
    val pendingIntent = PendingIntent.getBroadcast(
      this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
    )
    activityRecognitionClient.requestActivityUpdates(
      5000L, // every 5 seconds //TODO: Make it as a parameter
      pendingIntent
    )
    println("[LocationService] Activity updates started.")
  }

  private fun showNotification() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      println("[LocationService] Showing notification...")
      val channel = NotificationChannel(
        "location_channel",
        "Location Tracking",
        NotificationManager.IMPORTANCE_LOW
      )
      val notificationManager = getSystemService(NotificationManager::class.java)
      notificationManager.createNotificationChannel(channel)
      val notification = NotificationCompat.Builder(this, "location_channel")
        .setContentTitle("Location Tracker")
        .setContentText("Tracking your location in the background")
        .setSmallIcon(android.R.drawable.ic_menu_mylocation)
        .build()
      startForeground(1, notification)
      println("[LocationService] Notification shown.")
    }
  }

  private fun startLocationUpdates() {
    println("[LocationService] Starting location updates...")
    val prefs = getSharedPreferences("location_prefs", Context.MODE_PRIVATE)
    val activityType = prefs.getInt("last_activity", DetectedActivity.STILL)
    val locationRequestBuilder = LocationRequest.Builder(
      when (activityType) {
        DetectedActivity.IN_VEHICLE -> Priority.PRIORITY_BALANCED_POWER_ACCURACY
        DetectedActivity.ON_BICYCLE -> Priority.PRIORITY_BALANCED_POWER_ACCURACY
        DetectedActivity.RUNNING -> Priority.PRIORITY_BALANCED_POWER_ACCURACY
        DetectedActivity.WALKING -> Priority.PRIORITY_BALANCED_POWER_ACCURACY
        DetectedActivity.STILL -> Priority.PRIORITY_LOW_POWER
        else -> Priority.PRIORITY_BALANCED_POWER_ACCURACY
      },
      when (activityType) {
        DetectedActivity.IN_VEHICLE -> 6000L // 6 secs
        DetectedActivity.ON_BICYCLE -> 6000L // 6 secs
        DetectedActivity.RUNNING -> 6000L // 6 secs
        DetectedActivity.WALKING -> 12000L // 12 secs
        DetectedActivity.STILL -> 120000L // 120 secs
        else -> 60000L // 60 secs
      }
    ).setMinUpdateDistanceMeters(
      when (activityType) {
        DetectedActivity.IN_VEHICLE -> 20f // 20 metres
        DetectedActivity.ON_BICYCLE -> 20f // 20 metres
        DetectedActivity.RUNNING -> 20f // 20 metres
        DetectedActivity.WALKING -> 10f // 10 metres
        DetectedActivity.STILL -> 100f // 100 metres
        else -> 20f // 20 metres
      }
    )

    val request = locationRequestBuilder.build()
    locationCallback = object : LocationCallback() {
      override fun onLocationResult(result: LocationResult) {
        for (location in result.locations) {
          println("[LocationService] New location update: ${location.latitude}, ${location.longitude}")
          Logger.logLocation(location.latitude, location.longitude)
        }
      }
    }

    try {
      println("[LocationService] Requesting location updates...")
      fusedClient.requestLocationUpdates(
        request,
        locationCallback,
        Looper.getMainLooper()
      )
    } catch (ex: SecurityException) {
      println("[LocationService] SecurityException")
      ex.printStackTrace()
    }
    println("[LocationService] Location updates requested.")
  }

  override fun onDestroy() {
    super.onDestroy()
    println("[LocationService] Removing location updates...")
    fusedClient.removeLocationUpdates(locationCallback)
    println("[LocationService] Location updates removed.")
    println("[LocationService] Removing heartbeatLogger...")
    handler.removeCallbacks(heartbeatLogger)
    println("[LocationService] HeartbeatLogger removed.")
  }

  override fun onBind(intent: Intent?): IBinder? = null
}
