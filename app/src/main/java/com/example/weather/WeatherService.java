package com.example.weather;

import android.app.Service;
import android.content.Intent;
import android.content.res.Resources;
import android.location.Location;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WeatherService extends Service {
    private static final String TAG = WeatherService.class.getSimpleName();

    private ExecutorService executorService;

    private final IBinder binder = new WeatherBinder();

    private Handler handler;

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

    public void getCurrentWeatherInfo(@NonNull WeatherRequest weatherRequest, @NonNull WeatherServiceCallback callback) {
        Log.i(TAG, "getCurrentWeatherInfo");
        WeatherProviderRunnable weatherProviderRunnable = new WeatherProviderRunnable(weatherRequest, callback);
        executorService.execute(weatherProviderRunnable);
    }

    private class WeatherProviderRunnable implements Runnable {
        private final WeatherRequest weatherRequest;
        private final WeatherServiceCallback callback;

        public WeatherProviderRunnable(@NonNull WeatherRequest weatherRequest, @NonNull WeatherServiceCallback callback) {
            this.weatherRequest = weatherRequest;
            this.callback = callback;
        }

        public void run() {
            Log.d(TAG, "run");
            handler = new Handler(getMainLooper());

            WeatherInfoProvider weatherInfoProvider = new WeatherInfoProvider(weatherRequest);
            weatherInfoProvider.provideWeather(new WeatherInfoProvider.RequestCallback() {
                @Override
                public void onRequestSucceed(@NonNull WeatherInfo weatherInfo) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onWeatherInfoObtained(weatherInfo);
                        }
                    });
                }

                @Override
                public void onRequestFailed() {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onError();;
                        }
                    });
                }
            });
        }
    }

    public interface WeatherServiceCallback {
        void onWeatherInfoObtained(@NonNull WeatherInfo weatherInfo);
        void onError();
    }
}
