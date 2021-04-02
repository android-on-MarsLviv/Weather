package com.example.weather;

public class WeatherInfo {
    private final String temperature;
    private final String humidity;
    private final String visibility;
    private final String windSpeed;

    WeatherInfo(String temperature, String humidity, String visibility, String windSpeed) {
        this.temperature = temperature;
        this.humidity = humidity;
        this.visibility = visibility;
        this.windSpeed = windSpeed;
    }

    public String getTemperature() {
        return temperature;
    }

    public String getHumidity() {
        return humidity;
    }

    public String getVisibility() {
        return visibility;
    }

    public String getWindSpeed() { return windSpeed;  }
}
