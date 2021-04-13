package com.example.weather;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
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
    private /*final*/ LocationCallback locationCallback;

    private /*final*/ RetrieveLocationCallback retrieveLocationCallback;
    private final Activity activity;

    static final int LOCATION_REQUEST = 1000;

    public LocationClient(@NonNull Activity activity) {
        this.activity = activity;

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity);

        locationRequest = LocationRequest.create();
        locationRequest.setNumUpdates(1);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    public void getLocation(@NonNull RetrieveLocationCallback retrieveLocationCallback) {
        if (!checkPermission()) {
            requestPermission();
            return;
        }

        this.retrieveLocationCallback = retrieveLocationCallback;

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                retrieveLocationCallback.onRetrieveLocation(locationResult.getLastLocation());
            }
        };

        Log.i(TAG, "permissions already granted");
        getFusedLocation();
    }

    private boolean checkPermission() {
        return ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        Log.i(TAG, "request permissions");
        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_REQUEST);
    }

    @SuppressLint("MissingPermission")
    private void getFusedLocation() {
        fusedLocationClient.getLastLocation().addOnSuccessListener(activity, location -> {
            if (location == null) {
                Log.i(TAG, "location == null");
                fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
                return;
            }

            retrieveLocationCallback.onRetrieveLocation(location);
        }).addOnCanceledListener(() -> Log.i(TAG, "location listener cancelled."))
                .addOnFailureListener((location) -> Log.i(TAG, "Location listener failed."));
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull int[] grantResults) {
        if (requestCode != LocationClient.LOCATION_REQUEST) {
            return;
        }

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getFusedLocation();
        } else {
            Toast.makeText(activity, "Permission denied", Toast.LENGTH_SHORT).show();
        }
    }

    public interface RetrieveLocationCallback {
        void onRetrieveLocation(@NonNull Location location);
    }
}
