class Latitude {

  final double value;

  static Latitude defaultLatitude = const Latitude._(0);
  const Latitude._(this.value);

  @override
  String toString() {
    return value.toString();
  }

  static Latitude fromDouble(double value) {
    return Latitude._(value);
  }

  bool equals(Latitude latitude) {
    return value == latitude.value;
  }

  static Latitude fromString(String value) {
    return Latitude._(double.parse(value));
  }

}