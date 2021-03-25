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

import java.util.Locale;
import java.util.concurrent.TimeUnit;

//import java.util.logging.Handler;

public class LocationClient implements LocationListener {

    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    double latitude;
    double longitude;

    Context context;

    public static final int LOCATION_REQUEST = 1000;
    public static final int GPS_REQUEST = 1001;

    public LocationClient(Context context) {
        this.context = context;

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10 * 1000); // 10 seconds
        locationRequest.setFastestInterval(5 * 1000); // 5 seconds

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

    public void getLocation(MyLocationCallback myLocationCallback) {
        new Handler(Looper.getMainLooper()).post(() -> {
            //latitude = 10.11;
            //longitude = 20.22;
            if (    ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_REQUEST);
                Log.d("TAG", "if case");
            } else {
                fusedLocationClient.getLastLocation().addOnSuccessListener((Activity) context, location -> {
                    if (location != null) {
                        latitude = location.getLatitude();Log.d("TAG", "lat 1:" + latitude);
                        longitude = location.getLongitude();Log.d("TAG", "lon 1:" + longitude);
                        myLocationCallback.onRetrieveLocation(latitude, longitude);
                    } else {
                        //fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
                        Log.d("TAG", "location == null");
                    }
                });
                Log.d("TAG", "else case");
            }
            /*try {
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }*/
            Log.d("TAG", "lat 2:" + latitude);
            Log.d("TAG", "lon 2:" + longitude);
//            myLocationCallback.onRetrieveLocation(latitude, longitude);
        });
    }

    //@Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d("TAG", "onRequestPermissionsResult");
        /*super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1000: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    if (isContinue) {
                        mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
                    } else {
                        mFusedLocationClient.getLastLocation().addOnSuccessListener(MainActivity.this, location -> {
                            if (location != null) {
                                wayLatitude = location.getLatitude();
                                wayLongitude = location.getLongitude();
                                txtLocation.setText(String.format(Locale.US, "%s - %s", wayLatitude, wayLongitude));
                            } else {
                                mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
                            }
                        });
                    }
                } else {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }*/
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {

    }
}
