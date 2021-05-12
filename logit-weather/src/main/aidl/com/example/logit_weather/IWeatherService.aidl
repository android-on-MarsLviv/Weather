// IWeatherService.aidl
package com.example.logit_weather;

import com.example.logit_weather.WeatherRequest;
import com.example.logit_weather.IWeatherServiceCallback;

interface IWeatherService {
    void getCurrentWeatherInfo(in WeatherRequest weatherRequest, in IWeatherServiceCallback callback);
}