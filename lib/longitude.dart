class Longitude {

  final double value;

  static Longitude defaultLongitude = const Longitude._(0);
  const Longitude._(this.value);

  @override
  String toString() {
    return value.toString();
  }

  static Longitude fromDouble(double value) {
    return Longitude._(value);
  }

  bool equals(Longitude longitude) {
    return value == longitude.value;
  }

  static Longitude fromString(String value) {
    return Longitude._(double.parse(value));
  }

}