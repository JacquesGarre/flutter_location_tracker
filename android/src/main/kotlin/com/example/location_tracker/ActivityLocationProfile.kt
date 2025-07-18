package com.example.location_tracker

import com.google.android.gms.location.DetectedActivity
import com.google.android.gms.location.Priority

class ActivityLocationProfile(private val type: Int) {

    fun label(): String = when (type) {
        DetectedActivity.IN_VEHICLE -> "In vehicle"
        DetectedActivity.ON_BICYCLE -> "On bicycle"
        DetectedActivity.ON_FOOT -> "On foot"
        DetectedActivity.RUNNING -> "Running"
        DetectedActivity.STILL -> "Still"
        DetectedActivity.TILTING -> "Tilting"
        DetectedActivity.UNKNOWN -> "Unknown"
        DetectedActivity.WALKING -> "Walking"
        else -> "Unknown"
    }

    fun priority(): Int = when (type) {
        DetectedActivity.STILL -> Priority.PRIORITY_LOW_POWER
        DetectedActivity.ON_BICYCLE, 
        DetectedActivity.ON_FOOT,
        DetectedActivity.RUNNING, 
        DetectedActivity.WALKING -> Priority.PRIORITY_HIGH_ACCURACY
        else -> Priority.PRIORITY_BALANCED_POWER_ACCURACY
    }

    fun intervalInMilliseconds(): Long = when (type) {
        DetectedActivity.STILL -> 100000L
        else -> 6000L
    }

    fun distanceInMeters(): Float = when (type) {
        DetectedActivity.WALKING -> 10f
        DetectedActivity.STILL -> 100f
        else -> 20f
    }
}