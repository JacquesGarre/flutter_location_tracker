import 'package:flutter/services.dart';

import 'location_tracker_platform_interface.dart';

class MethodChannelLocationTracker extends LocationTrackerPlatform {
  final methodChannel = const MethodChannel('location_tracker');

  @override
  Future<void> startService({
    required String notificationTitle,
    required String notificationContent,
    required int activityIntervalInMilliseconds,
  }) async {
    await methodChannel.invokeMethod('startService', {
      'notification_title': notificationTitle,
      'notification_content': notificationContent,
      'activity_interval_in_milliseconds': activityIntervalInMilliseconds,
    });
  }

  @override
  Future<void> stopService() async {
    await methodChannel.invokeMethod('stopService');
  }

  @override
  Future<String> getLogs() async {
    return await methodChannel.invokeMethod('getLogs');
  }

  @override
  Future<bool> isLocationPermissionAlwaysGranted() async {
    return await methodChannel.invokeMethod<bool>('isLocationPermissionAlwaysGranted') ?? false;
  }

  @override
  Future<bool> isNotificationPermissionGranted() async {
    return await methodChannel.invokeMethod<bool>('isNotificationPermissionGranted') ?? false;
  }

  @override
  Future<bool> isActivityRecognitionPermissionGranted() async {
    return await methodChannel.invokeMethod<bool>('isActivityRecognitionPermissionGranted') ?? false;
  }

  @override
  Future<void> openLocationPermissionPage() async {
    await methodChannel.invokeMethod<void>('openLocationPermissionPage');
  }

  @override
  Future<void> requestNotificationPermission() async {
    await methodChannel.invokeMethod<void>('requestNotificationPermission');
  }

  @override
  Future<void> requestActivityRecognitionPermission() async {
    await methodChannel.invokeMethod<void>('requestActivityRecognitionPermission');
  }

  @override
  Future<bool> isServiceRunning() async {
    return await methodChannel.invokeMethod<bool>('isServiceRunning') ?? false;
  }
}
