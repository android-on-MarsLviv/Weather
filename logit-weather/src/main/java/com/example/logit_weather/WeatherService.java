package com.example.logit_weather;

import android.app.Application;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WeatherService extends Service {
    private static final String TAG = WeatherService.class.getSimpleName();
    private static final String processName = Application.getProcessName();

    private ExecutorService executorService;

    IWeatherService.Stub binder = new IWeatherService.Stub() {
        @Override
        public void getCurrentWeatherInfo(WeatherRequest weatherRequest, IWeatherServiceCallback callback) {
            Log.i(TAG, "make binder");
            WeatherService.this.getCurrentWeatherInfo(weatherRequest, callback);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        executorService = Executors.newSingleThreadExecutor();

        Log.i(TAG, "onCreate: Process name: " + processName);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    private void getCurrentWeatherInfo(@NonNull WeatherRequest weatherRequest, IWeatherServiceCallback callback) {
        WeatherProviderRunnable weatherProviderRunnable = new WeatherProviderRunnable(weatherRequest, callback);
        executorService.execute(weatherProviderRunnable);
    }

    private class WeatherProviderRunnable implements Runnable {
        private final WeatherRequest weatherRequest;
        private final IWeatherServiceCallback callback;

        public WeatherProviderRunnable(@NonNull WeatherRequest weatherRequest, @NonNull IWeatherServiceCallback callback) {
            this.weatherRequest = weatherRequest;
            this.callback = callback;
        }

        public void run() {
            Log.i(TAG, "WeatherProviderRunnable: run");

            WeatherInfoProvider weatherInfoProvider = new WeatherInfoProvider(weatherRequest);
            weatherInfoProvider.provideWeather(new WeatherInfoProvider.RequestCallback() {
                @Override
                public void onRequestSucceed(@NonNull WeatherInfo weatherInfo) {
                    try {
                        callback.onWeatherInfoObtained(weatherInfo);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                        Log.e(TAG, "WeatherProviderRunnable: onRequestSucceed: couldn't make RequestCallback");
                    }
                }

                @Override
                public void onRequestFailed() {
                    try {
                        callback.onError();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                        Log.e(TAG, "WeatherProviderRunnable: onRequestFailed: couldn't make RequestCallback");
                    }
                }
            });
        }
    }
}
