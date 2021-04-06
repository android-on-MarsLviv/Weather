package com.example.weather;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Optional;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private TextView showWeatherView;
    private EditText editCityView;
    private Button weatherByCityButton;
    private Button weatherByLocationButton;

    private LocationClient locationClient;
    private Location currentLocation;

    WeatherService weatherService;
    boolean weatherServiceBound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        showWeatherView = findViewById(R.id.msg_temperature);
        editCityView = findViewById(R.id.msg_city);
        weatherByCityButton = findViewById(R.id.button_by_city);
        weatherByLocationButton = findViewById(R.id.button_by_location);

        weatherByCityButton.setOnClickListener(this::onClickByCity);
        weatherByLocationButton.setOnClickListener(this::onClickByLocation);

        locationClient = new LocationClient(this, new LocationClient.RetrieveLocationCallback() {
            @Override
            public void onRetrieveLocation(@NonNull Location location) {
                currentLocation = location;
                Log.d(TAG, "curr latitude:" + currentLocation.getLatitude() + "  curr longitude:" + currentLocation.getLongitude());
            }
        });
    }

    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            WeatherService.WeatherBinder binder = (WeatherService.WeatherBinder) service;
            weatherService = binder.getService();
            weatherServiceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            weatherServiceBound = false;
        }
    };

    @Override
    protected void onStart() {
        super.onStart();

        Intent intent = new Intent(this, WeatherService.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();

        unbindService(connection);
        weatherServiceBound = false;
    }

    private void onClickByCity(View view) {
        // TODO: keep this button disabled while current request not finished
        // https://trello.com/c/SFB76xJc
        Log.d(TAG, "onClick start");

        String cityName = editCityView.getText().toString();
        if (TextUtils.isEmpty(cityName)) {
            notificationOnError(getText(R.string.error_wrong_city).toString());
            return;
        }

        weatherService.getCurrentWeatherInfo(cityName, getResources(), new WeatherService.WeatherServiceCallback() {
            @Override
            public void onWeatherInfoObtained(Optional<WeatherInfo> weatherInfo) {
                if (!weatherInfo.isPresent()) {
                    Log.d(TAG, "weatherInfo is empty");
                    return;
                }
                WeatherInfo weather = weatherInfo.get();
                updateTemperatureView(getString(R.string.template_weather_message, weather.getTemperature(), weather.getVisibility(), weather.getHumidity(), weather.getWindSpeed()));
            }

            @Override
            public void onError(@NonNull Error errorCode) {
                notificationOnError(getText(R.string.error_wrong_request).toString());
            }
        });

        Log.d(TAG, "onClick finish");
    }

    public void onClickByLocation(View view) {
        // todo: implement
        // https://trello.com/c/W4VxNHog
        locationClient.getLocation();
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        locationClient.onRequestPermissionsResult(requestCode, grantResults);
    }

    private void updateTemperatureView(@NonNull String massage) {
        showWeatherView.post(new Runnable() {
            @Override
            public void run() {
                showWeatherView.setText(massage);
            }
        });
    }

    private void notificationOnError(@NonNull String notificationToUser) {
        updateTemperatureView(getText(R.string.default_weather_message).toString());
        runOnUiThread(() -> Toast.makeText(this, notificationToUser, Toast.LENGTH_SHORT).show());
    }
}