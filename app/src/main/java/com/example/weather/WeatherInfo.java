package com.example.weather;

public class WeatherInfo {
    private String temperature = "";
    private String humidity = "";
    private String visibility = "";
    private String weendSpeed = "";

    public boolean isEmpty = true;

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    public void setHumidity(String humidity) {
        this.humidity = humidity;
    }

    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }

    public void setWeendSpeed(String weendSpeed) {
        this.weendSpeed = weendSpeed;
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

    public String getWeendSpeed() {
        return weendSpeed;
    }
}
