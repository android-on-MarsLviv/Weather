package com.example.weather;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.Optional;

public class WeatherService extends Service {
    private static final String TAG = WeatherService.class.getSimpleName();

    private final IBinder binder = new WeatherBinder();

    public class WeatherBinder extends Binder {
        WeatherService getService() {
            return WeatherService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public void getCurrentWeatherInfo(@NonNull String cityName, @NonNull WeatherInfoCallback callback) {
        Log.d(TAG, "getCurrentWeatherInfo: City - " + cityName);
        //callback.onRequestSucceed(respond)
        callback.onWeatherInfoObtained(Optional.of(new WeatherInfo(
                                                "10",
                                                "99",
                                                "222",
                                                "0"))
        );
    }

    public void getCurrentWeatherInfo(@NonNull Location location, @NonNull WeatherInfoCallback callback) {
        Log.d(TAG, "getCurrentWeatherInfo: Location");

    }

    public interface WeatherInfoCallback {
        void onWeatherInfoObtained(Optional<WeatherInfo> weatherInfo);
        void onError(@NonNull Error errorCode);
    }
}
