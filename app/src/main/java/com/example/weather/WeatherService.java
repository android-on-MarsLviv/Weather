package com.example.weather;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;

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
        Log.d(TAG, "getCurrentWeatherInfo: City");

    }

    public void getCurrentWeatherInfo(@NonNull Location location, @NonNull WeatherInfoCallback callback) {
        Log.d(TAG, "getCurrentWeatherInfo: Location");

    }

    public interface WeatherInfoCallback {
        void onWeatherInfoObtained(@NonNull WeatherInfo info);
        void onError(@NonNull Error errorCode);
    }
}
