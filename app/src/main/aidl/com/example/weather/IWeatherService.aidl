// IWeatherService.aidl
package com.example.weather;

import com.example.weather.WeatherRequest;
import com.example.weather.IWeatherServiceCallback;

interface IWeatherService {
    void getCurrentWeatherInfo(in WeatherRequest weatherRequest, in IWeatherServiceCallback callback);
}