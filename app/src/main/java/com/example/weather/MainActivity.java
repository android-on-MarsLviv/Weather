package com.example.weather;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
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

    private TextView showWeatherView;
    private EditText editCityView;
    private Button weatherByCityButton;

    LocationGetter locationGetter;

    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        showWeatherView = findViewById(R.id.msg_temperature);
        editCityView = findViewById(R.id.msg_city);
        weatherByCityButton = findViewById(R.id.button_by_city);

        weatherByCityButton.setOnClickListener(this::onClickByCity);

        locationGetter = new LocationGetter(MainActivity.this);;

        context = this;
    }

    private void onClickByCity(View view) {
        // TODO: keep this button disabled while current request not finished
        // https://trello.com/c/SFB76xJc
        Log.d(TAG, "onClick start");

        final URL request;
        String cityName = editCityView.getText().toString();
        if (TextUtils.isEmpty(cityName)) {
            notificationOnError(context, getText(R.string.error_wrong_city).toString());
            return;
        }

        try {
            request = buildRequestUrlWithCity(cityName);
            Log.d(TAG, String.valueOf(request));
        } catch (MalformedURLException e) {
            notificationOnError(context, getText(R.string.error_wrong_request).toString(), e);
            return;
        }

        // todo: use Executors.newSingleThreadExecutor() executor to run network task on a separate thread.
        // https://trello.com/c/O0wDKeQP
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "run started");

                String respond = null;
                String temperature = null;
                try {
                    respond = doRequest(request);
                    if (TextUtils.isEmpty(respond)) {
                        notificationOnError(context, getText(R.string.error_wrong_request).toString());
                        return;
                    }

                    temperature = parseTemperature(respond);
                    if (TextUtils.isEmpty(temperature)) {
                        notificationOnError(context, getText(R.string.error_wrong_request).toString());
                        return;
                    }
                } catch (IOException | JSONException e) {
                    notificationOnError(context, getText(R.string.error_wrong_request).toString(), e);
                    return;
                }

                updateTemperatureView(getString(R.string.template_temperature_message, temperature));

                Log.d(TAG, "run finish");
            }
        }).start();

        Log.d(TAG, "onClick finish");
    }

    public void onClickByLocation(View view) {
        // todo: implement
        // https://trello.com/c/W4VxNHog
        Log.d(TAG, "onClickByLocation start");

        locationGetter.getLocation(new LocationCallback() {
            @Override
            public void onRetrieveLocation(double latitude, double longitude) {
                Log.d(TAG, "latitude:" + latitude + "  longitude:" + longitude);
            }
        });

        Log.d(TAG, "onClickByLocation finish");
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

    @Nullable
    private String doRequest(URL weatherEndpoint) throws IOException {
        Log.d(TAG, "doRequest start");
        String respond = null;

        InputStream stream = null;
        HttpsURLConnection connection = null;

        try {
            connection = (HttpsURLConnection) weatherEndpoint.openConnection();
            connection.setRequestMethod("GET");
            connection.setReadTimeout(3000);
            connection.connect();

            if (connection.getResponseCode() == HttpsURLConnection.HTTP_OK) {
                stream = connection.getInputStream();
                respond = StreamUtils.streamToString(stream);
            }
        } finally {
            StreamUtils.closeAll(stream);
            if (connection != null) {
                connection.disconnect();
            }
        }
        Log.d(TAG, "doRequest finish");
        return respond;
    }

    private void notificationOnError(Context context, String notificationToUser) {
        updateTemperatureView(getText(R.string.default_temperature_message).toString());
        runOnUiThread(() -> Toast.makeText(context, notificationToUser, Toast.LENGTH_SHORT).show());
    }

    private void notificationOnError(Context context, String notificationToUser, Exception exception) {
        updateTemperatureView(getText(R.string.default_temperature_message).toString());
        runOnUiThread(() -> Toast.makeText(context, notificationToUser, Toast.LENGTH_SHORT).show());
        exception.printStackTrace();
    }

}