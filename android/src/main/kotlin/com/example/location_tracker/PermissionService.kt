package com.example.location_tracker

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import android.content.Intent
import android.location.LocationManager
import android.net.Uri
import android.provider.Settings
import android.app.Activity
import androidx.core.app.ActivityCompat

object PermissionService {
    
    private const val NOTIFICATION_PERMISSION_REQUEST_CODE = 1002
    private const val ACTIVITY_RECOGNITION_PERMISSION_REQUEST_CODE = 1003

    fun hasForegroundLocationPermission(context: Context): Boolean {
        println("[PermissionService] start::hasForegroundLocationPermission()")
        val fineLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        println("[PermissionService] end::hasForegroundLocationPermission()")
        return fineLocation || coarseLocation
    }

    fun hasBackgroundLocationPermission(context: Context): Boolean {
        println("[PermissionService] hasBackgroundLocationPermission()")
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun isLocationPermissionAlwaysGranted(context: Context): Boolean {
        println("[PermissionService] isLocationPermissionAlwaysGranted()")
        return hasForegroundLocationPermission(context) &&
               hasBackgroundLocationPermission(context)
    }

    fun isNotificationPermissionGranted(context: Context): Boolean {
        println("[PermissionService] isNotificationPermissionGranted()")
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true 
        }
    }

    fun isActivityRecognitionPermissionGranted(context: Context): Boolean {
        println("[PermissionService] isActivityRecognitionPermissionGranted()")
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACTIVITY_RECOGNITION
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Not needed before Android 10
        }
    }

    fun openLocationPermissionPage(context: Context) {
        println("[PermissionService] start::openLocationPermissionPage()")
        try {
            println("[PermissionService] trying to open APP_LOCATION_SETTINGS")
            val intent = Intent("android.settings.APP_LOCATION_SETTINGS").apply {
                data = Uri.fromParts("package", context.packageName, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            println("[PermissionService] APP_LOCATION_SETTINGS successfully opened")
        } catch (e: Exception) {
            println("[PermissionService] failed to open APP_LOCATION_SETTINGS")
            println("[PermissionService] falling back to ACTION_APPLICATION_DETAILS_SETTINGS")
            val fallbackIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(fallbackIntent)
            println("[PermissionService] ACTION_APPLICATION_DETAILS_SETTINGS successfully opened")
        }
    }

    fun requestNotificationPermission(activity: Activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return;
        println("[PermissionService] start::requestNotificationPermission()")
        val granted = ActivityCompat.checkSelfPermission(
            activity,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
        if (!granted) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                NOTIFICATION_PERMISSION_REQUEST_CODE
            )
        }
        println("[PermissionService] end::requestNotificationPermission()")
    }


    fun requestActivityRecognitionPermission(activity: Activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return;
        println("[PermissionService] start::requestActivityRecognitionPermission()")
        val granted = ActivityCompat.checkSelfPermission(
            activity,
            Manifest.permission.ACTIVITY_RECOGNITION
        ) == PackageManager.PERMISSION_GRANTED
        if (!granted) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),
                ACTIVITY_RECOGNITION_PERMISSION_REQUEST_CODE
            )
        }
        println("[PermissionService] end::requestActivityRecognitionPermission()")
    }
}