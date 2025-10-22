package Marisoftware_Wheater_Data;

public class Location {
    private String weather;
    private double temperature;

    public Location(String location, double temperature) {
        this.weather = location;
        this.temperature = temperature;
    }

    public String getWeather() {
        return weather;
    }
    public double getTemperature() {
        return temperature;
    }

}
