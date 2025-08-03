import 'package:location_tracker/latitude.dart';
import 'package:location_tracker/longitude.dart';

class Location {
  final Latitude latitude;
  final Longitude longitude;

  const Location._(
    this.latitude,
    this.longitude,
  );

  static Location defaultLocation() {
    return Location._(
      Latitude.defaultLatitude, 
      Longitude.defaultLongitude,
    );
  }

  static Location fromLatitudeAndLongitude(
    Latitude latitude,
    Longitude longitude,
  ) {
    return Location._(
      latitude,
      longitude,
    );
  }
}
