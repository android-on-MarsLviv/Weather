package com.example.weather;

import android.location.Location;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class WeatherRequestData {
    private final String cityName;
    private final Location location;
    private final String weatherApiKey;
    private final String weatherApiEntryPoint;

    WeatherRequestData(@NonNull String cityName, @NonNull String weatherApiKey, @NonNull String weatherApiEntryPoint) {
        this.cityName = cityName;
        this.weatherApiKey = weatherApiKey;
        this.weatherApiEntryPoint = weatherApiEntryPoint;
        this.location = null;
    }

    WeatherRequestData(@NonNull Location location, @NonNull String weatherApiKey, @NonNull String weatherApiEntryPoint) {
        this.location = location;
        this.weatherApiKey = weatherApiKey;
        this.weatherApiEntryPoint = weatherApiEntryPoint;
        this.cityName = null;
    }

    @Nullable
    public String getCityName() {
        return cityName;
    }

    @Nullable
    public Location getLocation() {
        return location;
    }

    @NonNull
    public String getWeatherApiKey() {
        return weatherApiKey;
    }

    @NonNull
    public String getWeatherApiEntryPoint() {
        return weatherApiEntryPoint;
    }
}
