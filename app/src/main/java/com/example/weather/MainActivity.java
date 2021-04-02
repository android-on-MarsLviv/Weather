package com.example.weather;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int HTTP_REQUEST_TIMEOUT = 3000;

    private TextView showWeatherView;
    private EditText editCityView;
    private Button weatherByCityButton;
    private Button weatherByLocationButton;

    private LocationClient locationClient;
    private Location currentLocation;

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

    private void onClickByCity(View view) {
        // TODO: keep this button disabled while current request not finished
        // https://trello.com/c/SFB76xJc
        Log.d(TAG, "onClick start");

        final URL request;
        String cityName = editCityView.getText().toString();
        if (TextUtils.isEmpty(cityName)) {
            notificationOnError(getText(R.string.error_wrong_city).toString());
            return;
        }

        try {
            request = buildRequestUrlWithCity(cityName);
            Log.d(TAG, String.valueOf(request));
        } catch (MalformedURLException e) {
            notificationOnError(getText(R.string.error_wrong_request).toString(), e);
            return;
        }

        // todo: use Executors.newSingleThreadExecutor() executor to run network task on a separate thread.
        // https://trello.com/c/O0wDKeQP
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "run started");

                try {
                    doRequest(request, new RequestCallback() {
                        @Override
                        public void onRequestSucceed(String respond) {
                            try {
                                String temperature = parseTemperature(respond);
                                updateTemperatureView(getString(R.string.template_temperature_message, temperature));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onRequestFailed() {
                            notificationOnError(getText(R.string.error_wrong_request).toString());
                        }
                    });
                } catch (IOException e) {
                    notificationOnError(getText(R.string.error_wrong_request).toString(), e);
                    return;
                }

                Log.d(TAG, "run finish");
            }
        }).start();

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

    private void updateTemperatureView(String massage) {
        showWeatherView.post(new Runnable() {
            @Override
            public void run() {
                showWeatherView.setText(massage);
            }
        });
    }

    private URL buildRequestUrlWithCity(String cityName) throws MalformedURLException {
        Uri builtUri = Uri.parse(getText(R.string.weather_api_entry_point).toString())
                .buildUpon()
                .appendQueryParameter("q", cityName)
                .appendQueryParameter("appid", getText(R.string.weather_api_key).toString())
                .appendQueryParameter("units", "metric")
                .build();
        return new URL(builtUri.toString());
    }

    @Nullable
    private String parseTemperature(String response) throws JSONException {
        String temperature = null;

        JSONObject json = new JSONObject(response);
        JSONObject main  = json.getJSONObject("main");
        temperature = main.getString("temp");

        return temperature;
    }

    void doRequest(@NonNull URL weatherEndpoint, @NonNull RequestCallback callback) throws IOException {
        Log.d(TAG, "doRequest start");
        final String respond;

        InputStream stream = null;
        HttpsURLConnection connection = null;

        try {
            connection = (HttpsURLConnection) weatherEndpoint.openConnection();
            connection.setRequestMethod("GET");
            connection.setReadTimeout(HTTP_REQUEST_TIMEOUT);
            connection.connect();

            if (connection.getResponseCode() == HttpsURLConnection.HTTP_OK) {
                stream = connection.getInputStream();
                respond = StreamUtils.streamToString(stream);

                new Handler(Looper.getMainLooper()).post(() -> {
                    callback.onRequestSucceed(respond);
                });
            } else {
                new Handler(Looper.getMainLooper()).post(() -> {
                    callback.onRequestFailed();
                });
            }
        } finally {
            StreamUtils.closeAll(stream);
            if (connection != null) {
                connection.disconnect();
            }
        }
        Log.d(TAG, "doRequest finish");
    }

    private void notificationOnError(String notificationToUser) {
        updateTemperatureView(getText(R.string.default_temperature_message).toString());
        runOnUiThread(() -> Toast.makeText(this, notificationToUser, Toast.LENGTH_SHORT).show());
    }

    private void notificationOnError(String notificationToUser, Exception exception) {
        updateTemperatureView(getText(R.string.default_temperature_message).toString());
        runOnUiThread(() -> Toast.makeText(this, notificationToUser, Toast.LENGTH_SHORT).show());
        exception.printStackTrace();
    }

    public interface RequestCallback {
        void onRequestSucceed(String respond);
        void onRequestFailed();
    }

}