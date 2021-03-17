package com.example.weather;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.MalformedInputException;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private TextView widgetViewWeather;
    private EditText widgetEditCity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        widgetViewWeather = findViewById(R.id.msg_temperature);
        widgetEditCity = findViewById(R.id.msg_city);
    }

    public void onClickByCity(View view) {
        // TODO: keep this button disabled while current request not finished
        // https://trello.com/c/SFB76xJc
        Log.d(TAG, "OnClick start");

        final URL request;
        String cityName = widgetEditCity.getText().toString();
        if (TextUtils.isEmpty(cityName)) {
            msgOnWrongCity();
            return;
        }

        try {
            request = buildRequestUrlWithCity(cityName);
            Log.d(TAG, String.valueOf(request));
        } catch (MalformedURLException e) {
            e.printStackTrace();
            msgOnWrongCity();
            return;
        }

        // todo: use Executors.newSingleThreadExecutor() executor to run network task on a separate thread.
        // https://trello.com/c/O0wDKeQP
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "run started");
                String respond = null;
                try {
                    respond = doRequest(request);
                    if (TextUtils.isEmpty(respond)) {
                        msgOnWrongRequest();
                        return;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d(TAG, "request Error");
                    updateTemperatureView(getText(R.string.error_wrong_request).toString());
                    return;
                }

                String temperature = null;
                try {
                    temperature = parseTemperature(respond);
                    if (TextUtils.isEmpty(temperature)) {
                        msgOnWrongRequest();
                        return;
                    }
                } catch (JSONException e) {
                    updateTemperatureView(getText(R.string.error_wrong_city).toString());
                    Log.d(TAG, "parseTemperature Error");
                    return;
                }

                String temperatureTemplate = new String((String) getText(R.string.template_temperature_message));
                String temperatureMessage = String.format(temperatureTemplate, temperature);
                updateTemperatureView(temperatureMessage);

                Log.d(TAG, "run finish");
            }
        }).start();

        Log.d(TAG, "OnClick finish");
    }

    public void onClickByLocation(View view) {
        // todo: implement
        // https://trello.com/c/W4VxNHog
    }

    private void updateTemperatureView(String massage) {
        widgetViewWeather.post(new Runnable() {
            @Override
            public void run() {
                widgetViewWeather.setText(massage);
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

        BufferedReader reader = null;
        InputStream stream = null;
        HttpsURLConnection connection = null;

        try {
            connection = (HttpsURLConnection) weatherEndpoint.openConnection();
            connection.setRequestProperty("User-Agent", getText(R.string.app_name).toString());
            connection.setRequestMethod("GET");
            connection.setReadTimeout(3000);
            connection.connect();

            if (connection.getResponseCode() == HttpsURLConnection.HTTP_OK) {
                stream = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(stream));
                StringBuilder buf = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    buf.append(line).append("\n");
                }
                respond = buf.toString();
            }
        } finally {
            if (reader != null) {
                reader.close();
            }
            if (stream != null) {
                stream.close();
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
        Log.d(TAG, "doRequest finish");
        return respond;
    }

    private void msgOnWrongCity() {
        widgetViewWeather.setText(getText(R.string.error_wrong_city));
    }

    private void msgOnWrongRequest() {
        widgetViewWeather.setText(getText(R.string.error_wrong_request));
    }
}