import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'location_tracker_method_channel.dart';

abstract class LocationTrackerPlatform extends PlatformInterface {
  LocationTrackerPlatform() : super(token: _token);

  static final Object _token = Object();

  static LocationTrackerPlatform _instance = MethodChannelLocationTracker();

  static LocationTrackerPlatform get instance => _instance;

  static set instance(LocationTrackerPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<void> startService({
    required String notificationTitle,
    required String notificationContent,
    required int activityIntervalInMilliseconds,
  });

  Future<void> stopService();

  Future<String> getLogs();

  Future<bool> isLocationPermissionAlwaysGranted();

  Future<bool> isNotificationPermissionGranted();

  Future<bool> isActivityRecognitionPermissionGranted();

  Future<void> openLocationPermissionPage();

  Future<void> requestNotificationPermission();

  Future<void> requestActivityRecognitionPermission();
}
