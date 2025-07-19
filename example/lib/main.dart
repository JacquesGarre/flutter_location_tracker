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

class _MyAppState extends State<MyApp> with WidgetsBindingObserver {
  List<String> _logs = [];
  bool _locationPermissionAlwaysGranted = false;
  bool _notificationPermissionGranted = false;
  bool _activityRecognitionPermissionGranted = false;
  bool _isServiceRunning = false;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addObserver(this);
    _refreshState();
  }

  @override
  void dispose() {
    WidgetsBinding.instance.removeObserver(this);
    super.dispose();
  }

  @override
  void didChangeAppLifecycleState(AppLifecycleState state) {
    if (state == AppLifecycleState.resumed) {
      _refreshState();
    }
  }

  Future<void> _refreshState() async {
    final locationPermissionAlwaysGranted =
        await LocationTracker.isLocationPermissionAlwaysGranted();
    final notificationPermissionGranted =
        await LocationTracker.isNotificationPermissionGranted();
    final activityRecognitionPermissionGranted =
        await LocationTracker.isActivityRecognitionPermissionGranted();
    final isServiceRunning = await LocationTracker.isServiceRunning();
    final logs = await LocationTracker.getLogs();
    setState(() {
      _locationPermissionAlwaysGranted = locationPermissionAlwaysGranted;
      _notificationPermissionGranted = notificationPermissionGranted;
      _activityRecognitionPermissionGranted =
          activityRecognitionPermissionGranted;
      _isServiceRunning = isServiceRunning;
      _logs = logs.reversed.take(10).toList();
    });
  }

  Future<void> _startTracking() async {
    debugPrint("_startTracking");
    if (!_locationPermissionAlwaysGranted) {
      _showSnackbar('Location permission is required to start tracking.');
      await LocationTracker.openLocationPermissionPage();
      await _refreshState();
      return;
    }
    if (!_notificationPermissionGranted) {
      _showSnackbar('Notification permission is required to start tracking.');
      await LocationTracker.requestNotificationPermission();
      await _refreshState();
      return;
    }
    if (!_activityRecognitionPermissionGranted) {
      _showSnackbar('Activity permission is required to start tracking.');
      await LocationTracker.requestActivityRecognitionPermission();
      await _refreshState();
      return;
    }

    try {
      await LocationTracker.start(
        notificationTitle: "Location tracker example app",
        notificationContent: "Tracking your location in the background",
        activityIntervalInMilliseconds: 1000,
      );
      _refreshState();
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
            onPressed: _refreshState,
            tooltip: 'Refresh',
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
              'Location tracker running: $_isServiceRunning',
              style: const TextStyle(fontWeight: FontWeight.bold),
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
