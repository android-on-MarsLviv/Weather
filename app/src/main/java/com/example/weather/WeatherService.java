package com.example.weather;

import android.app.Service;
import android.content.Intent;
import android.content.res.Resources;
import android.location.Location;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WeatherService extends Service {
    private static final String TAG = WeatherService.class.getSimpleName();

    private ExecutorService executorService;

    private final IBinder binder = new WeatherBinder();

    @Override
    public void onCreate() {
        super.onCreate();
        executorService = Executors.newSingleThreadExecutor();
    }

    public class WeatherBinder extends Binder {
        WeatherService getService() {
            return WeatherService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public void getCurrentWeatherInfo(@NonNull WeatherRequestData weatherRequestData, WeatherServiceCallback callback) {
        Log.d(TAG, "getCurrentWeatherInfo: City - " + weatherRequestData.getCityName());

        WeatherServiceRunnable weatherServiceRunnable = new WeatherServiceRunnable(weatherRequestData, callback);
        executorService.execute(weatherServiceRunnable);
    }

    public void getCurrentWeatherInfo(@NonNull Location location, @NonNull Resources resources, WeatherServiceCallback callback) {
        Log.d(TAG, "getCurrentWeatherInfo: Location");

    }

    private class WeatherServiceRunnable implements Runnable {
        private WeatherRequestData weatherRequestData;
        private WeatherServiceCallback callback;

        public WeatherServiceRunnable(@NonNull WeatherRequestData weatherRequestData, WeatherServiceCallback callback) {
            this.weatherRequestData = weatherRequestData;
            this.callback = callback;
        }

        public void run() {
            WeatherRequest weatherRequest = new WeatherRequest(weatherRequestData);

            try {
                URL request = weatherRequest.buildRequestUrlByCity();
                weatherRequest.doRequest(request, new WeatherRequest.RequestCallback() {
                    @Override
                    public void onRequestSucceed(@NonNull String respond) {
                        Optional<WeatherInfo> weatherInfo = weatherRequest.parseWeather(respond);
                        if (!weatherInfo.isPresent()) {
                            Log.d(TAG, "weatherInfo is empty");
                            return;
                        }
                        WeatherInfo weather = weatherInfo.get();
                        callback.onWeatherInfoObtained(weather);
                    }

                    @Override
                    public void onRequestFailed() {
                        Log.d(TAG, "onRequestFailed");
                        callback.onError(new Error("onRequestFailed"));
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public interface WeatherServiceCallback {
        void onWeatherInfoObtained(WeatherInfo weatherInfo);
        void onError(@NonNull Error errorCode);
    }
}
