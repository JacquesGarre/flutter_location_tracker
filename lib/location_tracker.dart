import 'dart:convert';

import 'location_tracker_platform_interface.dart';

class LocationTracker {

  static Future<void> start() => LocationTrackerPlatform.instance.startService();

  static Future<void> stop() => LocationTrackerPlatform.instance.stopService();

  static Future<List<String>> getLogs() async {
    final jsonString = await LocationTrackerPlatform.instance.getLogs();
    final list = json.decode(jsonString) as List;
    return list.cast<String>();
  }
}
