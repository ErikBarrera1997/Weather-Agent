package Marisoftware_Wheater_Data;

/**
 * Ubicación geolocalizada del usuario: ciudad, código de país y coordenadas (lat/lon).
 */
public class UserLocation {
    private final String city;
    private final String countryCode;
    private final double latitude;
    private final double longitude;

    public UserLocation(String city, String countryCode, double latitude, double longitude) {
        this.city = city;
        this.countryCode = countryCode;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getCity() {
        return city;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    @Override
    public String toString() {
        return String.format("%s, %s (%.4f, %.4f)", city, countryCode, latitude, longitude);
    }
}
