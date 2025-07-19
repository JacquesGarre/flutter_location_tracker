import 'package:flutter/material.dart';
import 'dart:async';
import 'package:location_tracker/location_tracker.dart';

void main() {
  FlutterError.onError = (FlutterErrorDetails details) {
    debugPrint(details.exception.toString());
  };
  runZonedGuarded(() {
    WidgetsFlutterBinding.ensureInitialized();
    runApp(const MaterialApp(home: MyApp()));
  }, (error, stack) {
    debugPrint('Global error: $error\n$stack');
  });
}

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  List<String> _logs = [];
  bool _locationPermissionAlwaysGranted = false;
  bool _notificationPermissionGranted = false;
  bool _activityRecognitionPermissionGranted = false;

  @override
  void initState() {
    super.initState();
    _refresh();
  }

  Future<void> refreshPermissions() async {
    final locationPermissionAlwaysGranted = await LocationTracker.isLocationPermissionAlwaysGranted();
    final notificationPermissionGranted = await LocationTracker.isNotificationPermissionGranted();
    final activityRecognitionPermissionGranted = await LocationTracker.isActivityRecognitionPermissionGranted();
    setState(() {
      _locationPermissionAlwaysGranted = locationPermissionAlwaysGranted;
      _notificationPermissionGranted = notificationPermissionGranted;
      _activityRecognitionPermissionGranted =
          activityRecognitionPermissionGranted;
    });
  }

  Future<void> _startTracking() async {
    debugPrint("_startTracking");
    if (!_locationPermissionAlwaysGranted) {
      _showSnackbar('Location permission is required to start tracking.');
      await LocationTracker.openLocationPermissionPage();
      await refreshPermissions();
      return;
    }
    if (!_notificationPermissionGranted) {
      _showSnackbar('Notification permission is required to start tracking.');
      await LocationTracker.requestNotificationPermission();
      await refreshPermissions();
      return;
    }
    if (!_activityRecognitionPermissionGranted) {
      _showSnackbar('Activity permission is required to start tracking.');
      await LocationTracker.requestActivityRecognitionPermission();
      await refreshPermissions();
      return;
    }

    try {
      await LocationTracker.start(
        notificationTitle: "Location tracker example app",
        notificationContent: "Tracking your location in the background",
        activityIntervalInMilliseconds: 1000,
      );
      debugPrint("Location tracking started.");
      _showSnackbar('Location tracking started.');
    } catch (e) {
      debugPrint('Failed to start tracking: $e');
      _showSnackbar('Failed to start tracking: $e');
    }
  }

  Future<void> _stopTracking() async {
    try {
      await LocationTracker.stop();
      _showSnackbar('Location tracking stopped.');
    } catch (e) {
      _showSnackbar('Failed to stop tracking: $e');
    }
  }

  Future<void> _refresh() async {
    try {
      await refreshPermissions();
      final all = await LocationTracker.getLogs();
      setState(() {
        _logs = all.reversed.take(10).toList();
      });
    } catch (e) {
      _showSnackbar('Failed to load logs.');
    }
  }

  void _showSnackbar(String message) {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(content: Text(message)),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Location Tracker'),
        actions: [
          IconButton(
            icon: const Icon(Icons.refresh),
            onPressed: _refresh,
            tooltip: 'Reload logs',
          )
        ],
      ),
      body: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          children: [
            const SizedBox(height: 12),
            ElevatedButton(
              onPressed: _startTracking,
              child: const Text('Start Location Tracking'),
            ),
            const SizedBox(height: 12),
            ElevatedButton(
              onPressed: _stopTracking,
              child: const Text('Stop Location Tracking'),
            ),
            const SizedBox(height: 24),
            Text(
              'Location permission granted: $_locationPermissionAlwaysGranted',
              style: const TextStyle(fontWeight: FontWeight.bold),
            ),
            const SizedBox(height: 24),
            Text(
              'Notification permission granted: $_notificationPermissionGranted',
              style: const TextStyle(fontWeight: FontWeight.bold),
            ),
            const SizedBox(height: 24),
            Text(
              'Activity permission granted: $_activityRecognitionPermissionGranted',
              style: const TextStyle(fontWeight: FontWeight.bold),
            ),
            const SizedBox(height: 24),
            const Text(
              'Last 10 logs:',
              style: TextStyle(fontWeight: FontWeight.bold),
            ),
            const SizedBox(height: 12),
            Expanded(
              child: _logs.isEmpty
                  ? const Center(child: Text('No logs recorded yet.'))
                  : ListView.builder(
                      itemCount: _logs.length,
                      itemBuilder: (context, index) {
                        final log = _logs[index];
                        return ListTile(
                          leading: const Icon(Icons.location_on),
                          title: Text(log),
                        );
                      },
                    ),
            ),
          ],
        ),
      ),
    );
  }
}
