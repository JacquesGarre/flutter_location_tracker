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
      if (!ActivityRecognitionResult.hasResult(intent)) return
      println("[ActivityReceiver] start::onReceive()")
      val result = ActivityRecognitionResult.extractResult(intent) ?: return
      val activity = result.mostProbableActivity
      val type = activity.type
      val confidence = activity.confidence
      Logger.init(context)
      Logger.logActivity(type, confidence)
      val prefs = context.getSharedPreferences("location_prefs", Context.MODE_PRIVATE)
      val previous = prefs.getInt("last_activity", -1)
      if (previous == type) return
      prefs.edit().putInt("last_activity", type).apply()
      val restartIntent = Intent("com.example.location_tracker.ACTION_UPDATE_LOCATION_PROFILE")
      context.sendBroadcast(restartIntent)
      println("[ActivityReceiver] end::onReceive()")
  }

}