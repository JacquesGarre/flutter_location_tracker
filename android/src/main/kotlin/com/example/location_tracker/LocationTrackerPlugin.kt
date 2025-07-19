package com.example.location_tracker

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.NonNull
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONException
import android.app.Activity
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding

class LocationTrackerPlugin : FlutterPlugin, MethodCallHandler, ActivityAware {
  private lateinit var channel: MethodChannel
  private lateinit var context: Context
  private var activity: Activity? = null

  override fun onAttachedToEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
    context = binding.applicationContext
    channel = MethodChannel(binding.binaryMessenger, "location_tracker")
    channel.setMethodCallHandler(this)
  }

  override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
    when (call.method) {
      "startService" -> startService(call, result)
      "stopService" -> stopService(result)
      "getLogs" -> getLogs(result)
      "isLocationPermissionAlwaysGranted" -> isLocationPermissionAlwaysGranted(result)
      "isNotificationPermissionGranted" -> isNotificationPermissionGranted(result)
      "isActivityRecognitionPermissionGranted" -> isActivityRecognitionPermissionGranted(result)
      "openLocationPermissionPage" -> openLocationPermissionPage(result)
      "requestNotificationPermission" -> requestNotificationPermission(result)
      "requestActivityRecognitionPermission" -> requestActivityRecognitionPermission(result)
      else -> result.notImplemented()
    }
  }

  override fun onAttachedToActivity(binding: ActivityPluginBinding) {
    activity = binding.activity
  }

  override fun onDetachedFromActivity() {
    activity = null
  }

  override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
    activity = binding.activity
  }

  override fun onDetachedFromActivityForConfigChanges() {
    activity = null
  }

  private fun startService(call: MethodCall, result: Result) {
    println("[LocationTrackerPlugin] start::startService()")
    val title = call.argument<String>("notification_title") ?: "Location tracker"
    val content = call.argument<String>("notification_content") ?: "Tracking your location in the background"
    val activityIntervalInMilliseconds = call.argument<Int>("activity_interval_in_milliseconds") ?: 2000
    val intent = Intent(context, LocationService::class.java).apply {
      putExtra("notification_title", title)
      putExtra("notification_content", content)
      putExtra("activity_interval_in_milliseconds", activityIntervalInMilliseconds)
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      context.startForegroundService(intent)
    } else {
      context.startService(intent)
    }
    println("[LocationTrackerPlugin] end::startService()")
    result.success(null)
  }

  private fun stopService(result: Result) {
    println("[LocationTrackerPlugin] start::stopService()")
    val intent = Intent(context, LocationService::class.java)
    context.stopService(intent)
    println("[LocationTrackerPlugin] end::stopService()")
    result.success(null)
  }

  private fun getLogs(result: Result) {
    println("[LocationTrackerPlugin] start::getLogs()")
    try {
      Logger.init(context)
      val jsonLogs = Logger.getLogs()
      println("[LocationTrackerPlugin] end::getLogs()")
      result.success(jsonLogs)
    } catch (e: Exception) {
      e.printStackTrace()
      result.success("[]")
    }
  }

  private fun isLocationPermissionAlwaysGranted(result: Result) {
    println("[LocationTrackerPlugin] isLocationPermissionAlwaysGranted()")
    result.success(PermissionService.isLocationPermissionAlwaysGranted(context))
  }

  private fun isNotificationPermissionGranted(result: Result) {
    println("[LocationTrackerPlugin] isNotificationPermissionGranted()")
    result.success(PermissionService.isNotificationPermissionGranted(context))
  }

  private fun isActivityRecognitionPermissionGranted(result: Result) {
    println("[LocationTrackerPlugin] isActivityRecognitionPermissionGranted()")
    result.success(PermissionService.isActivityRecognitionPermissionGranted(context))
  }

  private fun openLocationPermissionPage(result: Result) {
    println("[LocationTrackerPlugin] openLocationPermissionPage()")
    PermissionService.openLocationPermissionPage(context)
    result.success(null)
  }

  private fun requestNotificationPermission(result: Result) {
    println("[LocationTrackerPlugin] requestNotificationPermission()")
    activity?.let {
        PermissionService.requestNotificationPermission(it)
        result.success(null)
    } ?: run {
        result.error("NO_ACTIVITY", "Activity is not attached", null)
    }
  }

  private fun requestActivityRecognitionPermission(result: Result) {
    println("[LocationTrackerPlugin] requestActivityRecognitionPermission()")
    activity?.let {
        PermissionService.requestActivityRecognitionPermission(it)
        result.success(null)
    } ?: run {
        result.error("NO_ACTIVITY", "Activity is not attached", null)
    }
  }

  override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
    channel.setMethodCallHandler(null)
  }
}