package com.example.location_tracker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.google.android.gms.location.ActivityRecognitionResult
import com.google.android.gms.location.DetectedActivity
import com.google.android.gms.location.Priority

class ActivityReceiver : BroadcastReceiver() {

  override fun onReceive(context: Context, intent: Intent) {
    println("[ActivityReceiver] Receiving activity...")
    if (ActivityRecognitionResult.hasResult(intent)) {
      val result = ActivityRecognitionResult.extractResult(intent)
      if (result != null) {
        val activity = result.mostProbableActivity
        val type = activity.type
        val confidence = activity.confidence
        when (type) {
          DetectedActivity.IN_VEHICLE -> println("[ActivityReceiver] Activity received: IN VEHICLE (confidence: $confidence)")
          DetectedActivity.ON_BICYCLE -> println("[ActivityReceiver] Activity received: ON BICYCLE (confidence: $confidence)")
          DetectedActivity.RUNNING -> println("[ActivityReceiver] Activity received: RUNNING (confidence: $confidence)")
          DetectedActivity.WALKING -> println("[ActivityReceiver] Activity received: WALKING (confidence: $confidence)")
          DetectedActivity.STILL -> println("[ActivityReceiver] Activity received: STILL (confidence: $confidence)")
          else -> println("[ActivityReceiver] Unknown activity")
        }
        val prefs = context.getSharedPreferences("location_prefs", Context.MODE_PRIVATE)
        val previous = prefs.getInt("last_activity", -1)
        if (previous != activity.type) {
          println("[ActivityReceiver] Storing activity...")
          prefs.edit().putInt("last_activity", activity.type).apply()
          println("[ActivityReceiver] Activity stored.")
          println("[ActivityReceiver] Broadcasting update location profile action...")
          val restartIntent = Intent("com.example.location_tracker.ACTION_UPDATE_LOCATION_PROFILE")
          context.sendBroadcast(restartIntent)
          println("[ActivityReceiver] Update location profile action broadcasted.")
        }
      }
    }
  }

}