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

class LocationTrackerPlugin : FlutterPlugin, MethodCallHandler {
  private lateinit var channel: MethodChannel
  private lateinit var context: Context

  override fun onAttachedToEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
    context = binding.applicationContext
    channel = MethodChannel(binding.binaryMessenger, "location_tracker")
    channel.setMethodCallHandler(this)
  }

  override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
    when (call.method) {
      "startService" -> startService(result)
      "stopService" -> stopService(result)
      "getLogs" -> getLogs(result)
      else -> result.notImplemented()
    }
  }

  private fun startService(result: Result) {
    println("[LocationTrackerPlugin] Starting service...")
    val intent = Intent(context, LocationService::class.java)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      context.startForegroundService(intent)
    } else {
      context.startService(intent)
    }
    result.success(null)
  }

  private fun stopService(result: Result) {
    println("[LocationTrackerPlugin] Stopping service...")
    val intent = Intent(context, LocationService::class.java)
    context.stopService(intent)
    result.success(null)
  }

  private fun getLogs(result: Result) {
    try {
      Logger.init(context)
      val jsonLogs = Logger.getLogs()
      result.success(jsonLogs)
    } catch (e: Exception) {
      e.printStackTrace()
      result.success("[]")
    }
  }

  override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
    channel.setMethodCallHandler(null)
  }
}