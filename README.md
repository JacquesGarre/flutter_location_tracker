
# location_tracker
A Flutter plugin proof of concept to track geolocation in the background, even when the app is terminated, with the lowest battery consumption possible.

The plugin will adapt the location tracking profile depending on your type of activity.
### Types of activity and tracking profile:
 - **STILL** : PRIORITY_LOW_POWER, frequency of 120 seconds, distance of 100 meters
 - **WALKING** : PRIORITY_BALANCED_POWER_ACCURACY, frequency of 12 seconds, distance of 10 meters
 - **RUNNING** : PRIORITY_BALANCED_POWER_ACCURACY, frequency of 6 seconds, distance of 20 meters
 - **ON_BICYCLE** : PRIORITY_BALANCED_POWER_ACCURACY, frequency of 6 seconds, distance of 20 meters
 - **IN_VEHICLE** :  PRIORITY_BALANCED_POWER_ACCURACY, frequency of 6 seconds, distance of 20 meters

The plugin will log in shared preferences every change of activity (along with the confidence) and every change of location. It also logs a healthcheck every 30 seconds, to ensure the background service is running well.
  
## Installation

 1. Clone the project
`git clone https://github.com/JacquesGarre/flutter_location_tracker`
`cd flutter_location_tracker`
 2. Install the app
`flutter doctor` (To make sure everything is alright in your local environment)
`cd example`
`flutter clean`
`flutter pub get`
Then run it on your mobile device (or emulator)
