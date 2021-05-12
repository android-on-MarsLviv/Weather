// IWeatherServiceCallback.aidl
package com.example.logit_weather;

import com.example.logit_weather.WeatherInfo;
import com.example.logit_weather.IWeatherService;

oneway interface IWeatherServiceCallback {
    void onWeatherInfoObtained(in WeatherInfo weatherInfo);
    void onError();
}