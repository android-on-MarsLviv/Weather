package com.example.weather;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WeatherService extends Service {
    private static final String TAG = WeatherService.class.getSimpleName();

    private ExecutorService executorService;

    private Handler handler;

    IWeatherService.Stub binder = new IWeatherService.Stub() {
        @Override
        public void getCurrentWeatherInfo(WeatherRequest weatherRequest, IWeatherServiceCallback callback) {
            Log.i(TAG, "make binder");
            WeatherProviderRunnable weatherProviderRunnable = new WeatherProviderRunnable(weatherRequest, callback);
            executorService.execute(weatherProviderRunnable);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        executorService = Executors.newSingleThreadExecutor();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    private class WeatherProviderRunnable implements Runnable {
        private final WeatherRequest weatherRequest;
        private final IWeatherServiceCallback callback;

        public WeatherProviderRunnable(@NonNull WeatherRequest weatherRequest, @NonNull IWeatherServiceCallback callback) {
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
                    handler.post(() -> {
                        try {
                            callback.onWeatherInfoObtained(weatherInfo);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                            Log.d(TAG, "handler couldn't post to main looper");
                        }
                    });
                }

                @Override
                public void onRequestFailed() {
                    handler.post(() -> {
                        try {
                            callback.onError();
                        } catch (RemoteException e) {
                            e.printStackTrace();
                            Log.d(TAG, "handler couldn't post to main looper");
                        }
                    });
                }
            });
        }
    }
}
