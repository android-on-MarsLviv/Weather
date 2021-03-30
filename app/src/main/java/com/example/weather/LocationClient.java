package com.example.weather;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Looper;
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

    private final FusedLocationProviderClient fusedLocationClient;
    private final LocationRequest locationRequest;
    private final LocationCallback locationCallback;

    private final RetrieveLocationCallback retrieveLocationCallback;

    private final Context context;

    static final int LOCATION_REQUEST = 1000;

    public LocationClient(@NonNull Context context, RetrieveLocationCallback retrieveLocationCallback) {
        this.context = context;
        this.retrieveLocationCallback = retrieveLocationCallback;

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);

        locationRequest = LocationRequest.create();
        locationRequest.setNumUpdates(1);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        getFusedLocation();
                    }
                }
            }
        };
    }

    public void getLocation() {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_REQUEST);
                Log.i(TAG, "request permissions");
            } else {
                Log.i(TAG, "permissions already grunted");
                getFusedLocation();
            }
    }

    @SuppressLint("MissingPermission")
    private void getFusedLocation() {
        fusedLocationClient.getLastLocation().addOnSuccessListener((Activity) context, location -> {
            if (location != null) {
                retrieveLocationCallback.onRetrieveLocation(location);
            } else {
                Log.i(TAG, "location == null");
                fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
            }
        }).addOnCanceledListener(() -> {
            Log.i(TAG, "location listener cancelled.");
        }).addOnFailureListener((location) -> {
            Log.i(TAG, "Location listener failed.");
        });
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull int[] grantResults) {
        if (requestCode == LocationClient.LOCATION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getFusedLocation();
            } else {
                Toast.makeText(context, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public interface RetrieveLocationCallback {
        void onRetrieveLocation(Location location);
    }
}
