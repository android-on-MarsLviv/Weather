package com.example.weather;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.os.Looper;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationCallback;

public class LocationClient {

    private static final String TAG = LocationClient.class.getSimpleName();

    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    private double latitude;
    private double longitude;

    private Context context;

    static final int LOCATION_REQUEST = 1000;       // todo: find equivalent on android SDK

    public LocationClient(Context context) {
        this.context = context;

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);

        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);// PRIORITY_BALANCED_POWER_ACCURACY   PRIORITY_LOW_POWER   PRIORITY_NO_POWER
        locationRequest.setInterval(10 * 1000);
        locationRequest.setFastestInterval(5 * 1000);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                        fusedLocationClient.removeLocationUpdates(locationCallback);
                    }
                }
            }
        };
    }

    //public void getLocation(MyLocationCallback myLocationCallback) {
    public void getLocation(OnLocationCallback myLocationCallback) {
        new Handler(Looper.getMainLooper()).post(() -> {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&// check
                    ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_REQUEST);
                Log.d(TAG, "if case -> request permissions");
            } else {
                fusedLocationClient.getLastLocation().addOnSuccessListener((Activity) context, location -> {
                    if (location != null) {
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                        myLocationCallback.onRetrieveLocation(latitude, longitude);
                    } else {
                        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
                        Log.d(TAG, "location == null");
                    }
                }).addOnCanceledListener(() -> {
                    Log.d(TAG, "location listener cancelled");
                }).addOnFailureListener((location) -> {// check
                    Log.d(TAG, "location listener failed");
                });
                Log.d(TAG, "else case -> permissions already grunted");
            }
        });
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public interface OnLocationCallback {
        void onRetrieveLocation(double latitude, double longitude);
    }
}
