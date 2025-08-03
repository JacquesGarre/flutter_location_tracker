import 'dart:convert';

import 'package:location_tracker/latitude.dart';
import 'package:location_tracker/location.dart';
import 'package:location_tracker/longitude.dart';

import 'location_tracker_platform_interface.dart';

class LocationTracker {
  static Future<void> start({
    required String notificationTitle,
    required String notificationContent,
    required int activityIntervalInMilliseconds,
    bool debug = false
  }) =>
      LocationTrackerPlatform.instance.startService(
          notificationTitle: notificationTitle,
          notificationContent: notificationContent,
          activityIntervalInMilliseconds: activityIntervalInMilliseconds,
          debug: debug,
      );

  static Future<void> stop() => LocationTrackerPlatform.instance.stopService();

  static Future<List<String>> getLogs() async {
    final jsonString = await LocationTrackerPlatform.instance.getLogs();
    final list = json.decode(jsonString) as List;
    return list.cast<String>();
  }

  static Future<bool> isLocationPermissionAlwaysGranted() async {
    return await LocationTrackerPlatform.instance
        .isLocationPermissionAlwaysGranted();
  }

  static Future<bool> isNotificationPermissionGranted() async {
    return await LocationTrackerPlatform.instance
        .isNotificationPermissionGranted();
  }

  static Future<bool> isActivityRecognitionPermissionGranted() async {
    return await LocationTrackerPlatform.instance
        .isActivityRecognitionPermissionGranted();
  }

  static Future<void> openLocationPermissionPage() async {
    await LocationTrackerPlatform.instance.openLocationPermissionPage();
  }

  static Future<void> requestNotificationPermission() async {
    await LocationTrackerPlatform.instance.requestNotificationPermission();
  }

  static Future<void> requestActivityRecognitionPermission() async {
    await LocationTrackerPlatform.instance.requestActivityRecognitionPermission();
  }

  static Future<bool> isServiceRunning() async {
    return await LocationTrackerPlatform.instance.isServiceRunning();
  }

  static Future<Location> getCurrentLocation() async {
    final result = await LocationTrackerPlatform.instance.getCurrentLocation();
    if (result.isEmpty) return Location.defaultLocation();
    final latValue = (result['lat'] as num).toDouble();
    final lngValue = (result['lng'] as num).toDouble();
    return Location.fromLatitudeAndLongitude(
      Latitude.fromDouble(latValue),
      Longitude.fromDouble(lngValue), 
    );
  }
}
