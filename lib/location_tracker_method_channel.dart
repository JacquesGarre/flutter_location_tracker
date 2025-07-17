import 'package:flutter/services.dart';

import 'location_tracker_platform_interface.dart';

class MethodChannelLocationTracker extends LocationTrackerPlatform {
  final methodChannel = const MethodChannel('location_tracker');

  @override
  Future<void> startService() async {
    await methodChannel.invokeMethod('startService');
  }

  @override
  Future<void> stopService() async {
    await methodChannel.invokeMethod('stopService');
  }

  @override
  Future<String> getLogs() async {
    return await methodChannel.invokeMethod('getLogs');
  }
}
