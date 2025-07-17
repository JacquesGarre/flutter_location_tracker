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

  // TODOS: 
  // - Gracefully shutdown when a permission is disabled / location service disabled
  // - Ask for permissions within the plugin, when starting the plugin (and redirect to proper page if possible)
  // - Only use useful logs for more battery economy
  // - Pass parameters as arguments (refresh time, distance, accuracy, notification title, notification content)
  // - Decide what to do with the locations? is a stream the best option? Is there another way to save battery to the max??

  override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
    when (call.method) {
      
      "startService" -> {
        println("[LocationTrackerPlugin] Starting service...")
        val intent = Intent(context, LocationService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
          println("[LocationTrackerPlugin] context.startForegroundService...")
          context.startForegroundService(intent)
        } else {
          println("[LocationTrackerPlugin] context.startService...")
          context.startService(intent)
        }
        result.success(null)
      }

      "stopService" -> {
        println("[LocationTrackerPlugin] Stopping service...")
        val intent = Intent(context, LocationService::class.java)
        context.stopService(intent)
        result.success(null)
      }

      "getLogs" -> {
        println("[LocationTrackerPlugin] Getting logs...")
        try {
            Logger.init(context)
            val jsonLogs = Logger.getLogs()
            result.success(jsonLogs)
        } catch (e: Exception) {
            e.printStackTrace()
            result.success("[]")
        }
      }

      else -> result.notImplemented()
    }
  }

  override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
    channel.setMethodCallHandler(null)
  }
}