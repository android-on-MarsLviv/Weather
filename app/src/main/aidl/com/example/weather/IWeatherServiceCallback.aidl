// IWeatherServiceCallback.aidl
package com.example.weather;

import com.example.weather.WeatherInfo;
import com.example.weather.IWeatherService;

oneway interface IWeatherServiceCallback {
    void onWeatherInfoObtained(in WeatherInfo weatherInfo);
    void onError();
}