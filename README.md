
  

# Flutter Location Tracker

A Flutter plugin proof of concept to track geolocation in the background, even when the app is terminated, with the lowest battery consumption possible.

  

The plugin will adapt the location tracking profile depending on your type of activity.

### Types of activity and tracking profile:

-  **STILL** : PRIORITY_LOW_POWER, frequency of 120 seconds, distance of 100 meters

-  **WALKING** : PRIORITY_BALANCED_POWER_ACCURACY, frequency of 12 seconds, distance of 10 meters

-  **RUNNING** : PRIORITY_BALANCED_POWER_ACCURACY, frequency of 6 seconds, distance of 20 meters

-  **ON_BICYCLE** : PRIORITY_BALANCED_POWER_ACCURACY, frequency of 6 seconds, distance of 20 meters

-  **IN_VEHICLE** : PRIORITY_BALANCED_POWER_ACCURACY, frequency of 6 seconds, distance of 20 meters


The plugin will log in shared preferences every change of activity (along with the confidence) and every change of location. It also logs a healthcheck every 30 seconds, to ensure the background service is running well.

### Permissions needed to run properly:
```
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_LOCATION"  />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"  />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"  />
<uses-permission android:name="android.permission.ACCESS_BACKGROUND_LOCATION"  />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE"  />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS"  />
<uses-permission android:name="android.permission.ACTIVITY_RECOGNITION"  />
```

## Installation

  

**1. Clone the project**

-  `git clone https://github.com/JacquesGarre/flutter_location_tracker`

-  `cd flutter_location_tracker`

**2. Install the app**

-  `flutter doctor` (To make sure everything is alright in your local environment)

-  `cd example`

-  `flutter clean`

-  `flutter pub get`

- Then run it on your mobile device (or emulator)

**3. Authorize permissions**

- Go to the flutter_location_tracker_example app info > Authorizations/Permissions
- Accept **Physical activity**, **Notifications**, and **Location (Always)**

**4. Start the app**

- You should now be able to start the app. You can start and stop the background location tracker by clicking accordingly on the buttons in the UI. 
- Logs will be loaded everytime you open the app, you can also refresh them in the app top bar.


## Todos
- Gracefully shutdown the service when any permission gets disabled or if the location service gets disabled
- Generate a stream to listen to location changes from flutter app
- Add methods to open proper permissions pages (if possible)
- Pass parameters as arguments of the plugin (tracking profiles related to activity, notification title, notification content, etc...) so it can be configured from the flutter app
- Keep only use useful logs to save more battery
