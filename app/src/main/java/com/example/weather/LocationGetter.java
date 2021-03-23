package com.example.weather;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.os.Looper;
import android.os.Handler;

import androidx.annotation.NonNull;

import java.util.concurrent.TimeUnit;

//import java.util.logging.Handler;

public class LocationGetter implements LocationListener {

    double latitude;
    double longitude;

    public LocationGetter(Context context) {

    }

    @Override
    public void onLocationChanged(@NonNull Location location) {

    }

    public void getLocation(LocationCallback locationCallback) {

        new Handler(Looper.getMainLooper()).post(() -> {
            latitude = 10.11;
            longitude = 20.22;

            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            locationCallback.onRetrieveLocation(latitude, longitude);
        });
    }
}
