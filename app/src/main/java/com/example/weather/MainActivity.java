package com.example.weather;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    public String processName = Application.getProcessName();

    private TextView showWeatherView;
    private EditText editCityView;
    private Button weatherByCityButton;
    private Button weatherByLocationButton;

    private LocationClient locationClient;

    private IWeatherService weatherService;

    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            weatherService = IWeatherService.Stub.asInterface(service);
            Log.i(TAG, "WeatherService connected");
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            weatherService = null;
            Log.i(TAG, "WeatherService disconnected");
        }
    };

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

        locationClient = new LocationClient(this);

        Log.i(TAG, "Process name: " + processName);
    }

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
        weatherService = null;
    }

    private void onClickByCity(View view) {
        Log.i(TAG, "onClickByCity");
        weatherByCityButton.setEnabled(false);

        String cityName = editCityView.getText().toString();
        if (TextUtils.isEmpty(cityName)) {
            showWeatherView.setText(getText(R.string.error_wrong_city).toString());
            weatherByCityButton.setEnabled(true);
            return;
        }

        WeatherRequest weatherRequest = new WeatherRequest.Builder(getText(R.string.weather_api_key).toString(), getText(R.string.weather_api_entry_point).toString())
                .setCity(cityName)
                .build();

        try {
            weatherService.getCurrentWeatherInfo(weatherRequest, new IWeatherServiceCallback.Stub() {
                @Override
                public void onWeatherInfoObtained(@NonNull WeatherInfo weatherInfo) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showWeatherView.setText(getString(R.string.template_weather_message, weatherInfo.getTemperature(), weatherInfo.getVisibility(), weatherInfo.getHumidity(), weatherInfo.getWindSpeed()));
                            weatherByCityButton.setEnabled(true);
                        }
                    });
                }

                @Override
                public void onError() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showWeatherView.setText(getText(R.string.default_weather_message).toString());
                            Toast.makeText(MainActivity.this, getText(R.string.error_wrong_request).toString(), Toast.LENGTH_SHORT).show();
                            weatherByCityButton.setEnabled(true);
                        }
                    });
                    Log.i(TAG, "onError");
                }
            });
        } catch (RemoteException e) {
            e.printStackTrace();
            Log.i(TAG, "RemoteException during weather receive request");
        }
    }

    public void onClickByLocation(View view) {
        Log.i(TAG, "onClickByLocation");
        weatherByLocationButton.setEnabled(false);

        locationClient.getLocation(new LocationClient.RetrieveLocationCallback() {
            @Override
            public void onRetrieveLocation(@NonNull Location location) {
                Log.i(TAG, "onRetrieveLocation(): latitude:" + location.getLatitude() + "  longitude:" + location.getLongitude());

                WeatherRequest weatherRequest = new WeatherRequest.Builder(getText(R.string.weather_api_key).toString(), getText(R.string.weather_api_entry_point).toString())
                        .setLocation(location)
                        .build();
                try {
                    weatherService.getCurrentWeatherInfo(weatherRequest, new IWeatherServiceCallback.Stub() {
                        @Override
                        public void onWeatherInfoObtained(@NonNull WeatherInfo weatherInfo) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    showWeatherView.setText(getString(R.string.template_weather_message, weatherInfo.getTemperature(), weatherInfo.getVisibility(), weatherInfo.getHumidity(), weatherInfo.getWindSpeed()));
                                    editCityView.setText("");
                                    weatherByLocationButton.setEnabled(true);
                                }
                            });
                        }

                        @Override
                        public void onError() {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    showWeatherView.setText(getText(R.string.default_weather_message).toString());
                                    Toast.makeText(MainActivity.this, getText(R.string.error_wrong_request).toString(), Toast.LENGTH_SHORT).show();
                                    editCityView.setText("");
                                    weatherByLocationButton.setEnabled(true);
                                }
                            });
                            Log.i(TAG, "onError");
                        }
                    });
                } catch (RemoteException e) {
                    e.printStackTrace();
                    Log.i(TAG, "RemoteException during weather service request");
                }
            }
        });
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        locationClient.onRequestPermissionsResult(requestCode, grantResults);
    }
}