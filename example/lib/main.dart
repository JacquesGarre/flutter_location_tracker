import 'package:flutter/material.dart';
import 'dart:async';
import 'package:location_tracker/location_tracker.dart';
import 'package:permission_handler/permission_handler.dart';

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

  @override
  void initState() {
    super.initState();
    _loadLogs();
  }

  Future<void> requestPermissions() async {
    await [
      Permission.location,
      Permission.locationAlways,
      Permission.locationWhenInUse,
      Permission.notification,
      Permission.activityRecognition,
    ].request();
  }

  Future<void> _startTracking() async {
    debugPrint("_startTracking");
    final locationPermissionStatus = await Permission.locationAlways.status;
    if (!locationPermissionStatus.isGranted) {
      debugPrint(
          "location always permission not granted, requesting permissions...");
      await requestPermissions();
    }
    final notificationPermissionStatus = await Permission.notification.status;
    if (!notificationPermissionStatus.isGranted) {
      debugPrint(
          "notification permission not granted, requesting permissions...");
      await requestPermissions();
    }
    final activityRecognitionPermissionStatus =
        await Permission.activityRecognition.status;
    if (!activityRecognitionPermissionStatus.isGranted) {
      debugPrint(
          "activity recognition permission not granted, requesting permissions...");
      await requestPermissions();
    }

    final granted = await Permission.locationAlways.isGranted;
    if (!granted) {
      debugPrint("Location permission is required to start tracking.");
      _showSnackbar('Location permission is required to start tracking.');
      return;
    }

    try {
      await LocationTracker.start();
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

  Future<void> _loadLogs() async {
    try {
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
            onPressed: _loadLogs,
            tooltip: 'Reload logs',
          )
        ],
      ),
      body: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          children: [
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
