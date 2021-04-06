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

    ExecutorService executorService;

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

    public void getCurrentWeatherInfo(@NonNull String cityName, Resources resources, @NonNull WeatherInfoCallback callback) {
        Log.d(TAG, "getCurrentWeatherInfo: City - " + cityName);

        HandleRequest handleRequest = new HandleRequest(cityName, resources, callback);
        executorService.execute(handleRequest);
    }

    public void getCurrentWeatherInfo(@NonNull Location location, @NonNull Resources resources, @NonNull WeatherInfoCallback callback) {
        Log.d(TAG, "getCurrentWeatherInfo: Location");

    }

    private class HandleRequest implements Runnable {
        String cityName;
        Resources resources;
        WeatherInfoCallback callback;

        public HandleRequest(@NonNull String cityName, @NonNull Resources resources, @NonNull WeatherInfoCallback callback) {
            this.cityName = cityName;
            this.resources = resources;
            this.callback = callback;
        }

        public void run() {
            WeatherRequest weatherRequest = new WeatherRequest(resources);
            final URL request;

            try {
                request = weatherRequest.buildRequestUrl(cityName);
            } catch (MalformedURLException e) {
                e.printStackTrace();
                return;
            }

            try {
                weatherRequest.doRequest(request, new WeatherRequest.RequestCallback() {
                    @Override
                    public void onRequestSucceed(String respond) {
                        Optional<WeatherInfo> weatherInfo = weatherRequest.parseWeather(respond);
                        if (!weatherInfo.isPresent()) {
                            Log.d(TAG, "weatherInfo is empty");
                            return;
                        }
                        WeatherInfo weather = weatherInfo.get();

                        callback.onWeatherInfoObtained(Optional.of(weather));
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

    public interface WeatherInfoCallback {
        void onWeatherInfoObtained(Optional<WeatherInfo> weatherInfo);
        void onError(@NonNull Error errorCode);
    }
}
