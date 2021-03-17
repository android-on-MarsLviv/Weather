package com.example.weather;

import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
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
        Log.d("myTag", "OnClick start");

        final URL request;
        try {
            String city = widgetEditCity.getText().toString();
            checkCityString(city);

            request = buildRequestUrlWithCity(city);
            Log.d("myTag", String.valueOf(request));
        } catch (MalformedURLException | NullPointerException | IllegalArgumentException e) {
            e.printStackTrace();
            msgOnWrongCity();
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d("myTag", "run started");
                String respond = null;
                try {
                    respond = doRequest(request);
                } catch (IOException | NullPointerException | IllegalArgumentException e) {
                    e.printStackTrace();
                    Log.d("myTag", "request Error");
                    postMessageToViewWeather(getText(R.string.error_wrong_request).toString());
                    return;
                }

                String temperature = null;
                try {
                    temperature = parseTemperature(respond);
                } catch (NullPointerException | IllegalArgumentException | JSONException e) {
                    postMessageToViewWeather(getText(R.string.error_wrong_city).toString());
                    Log.d("myTag", "parseTemperature() Error");
                    return;
                }

                String temperatureTemplate = new String((String) getText(R.string.template_temperature_message));
                String temperatureMessage = String.format(temperatureTemplate, temperature);
                postMessageToViewWeather(temperatureMessage);
                Log.d("myTag", "Temp:" + temperatureMessage);

                Log.d("myTag", "run finished");
            }
        }).start();

        Log.d("myTag", "OnClick finish");
    }

    public void onClickByLocation(View view) {

    }

    private void postMessageToViewWeather(String massage) {
        widgetViewWeather.post(new Runnable() {
            @Override
            public void run() {
                widgetViewWeather.setText(massage);
            }
        });
    }

    private void checkCityString(String city) throws NullPointerException, IllegalArgumentException {
        if (city == null) {
            throw new NullPointerException("Argument is null at method checkCityString");
        }
        if (city.length() == 0) {
            throw new IllegalArgumentException("Argument have zero length at method checkCityString");
        }
        // TODO: do some other checking of input string
    }

    private URL buildRequestUrlWithCity(String city) throws MalformedURLException {
        Uri builtUri = Uri.parse(getText(R.string.weather_api_entry_point).toString())
                .buildUpon()
                .appendQueryParameter("q", city)
                .appendQueryParameter("appid", getText(R.string.weather_api_key).toString())
                .appendQueryParameter("units", "metric")
                .build();
        return new URL(builtUri.toString());
    }

    private String parseTemperature(String response) throws JSONException, NullPointerException, IllegalArgumentException {
        String temperature = null;

        JSONObject json = new JSONObject(response);
        JSONObject main  = json.getJSONObject("main");
        temperature = main.getString("temp");
        if (temperature == null) {
            throw new NullPointerException("Couldn't parse response at method parseTempetature");
        }
        if (temperature.length() == 0) {
            throw new IllegalArgumentException("Zero response length at method parseTempetature");
        }
        Log.d("myTag", "temperature = " + temperature);

        return temperature;
    }

    private String doRequest(URL weatherEndpoint) throws IOException, NullPointerException, IllegalArgumentException {
        Log.d("myTag", "doRequest start");
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
                if (respond == null) {
                    throw new NullPointerException("Got null String on respond in doRequest method.");
                }
                if (respond.length() == 0) {
                    throw new IllegalArgumentException("Got zero-length String on respond in doRequest method.");
                }
            } else {
                Log.d("myTag", "not 200");
                throw new IOException("Respond code is: " + connection.getResponseCode());
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
        Log.d("myTag", "doRequest finish");
        return respond;
    }

    private void msgOnWrongCity() {
        widgetViewWeather.setText(getText(R.string.error_wrong_city));
    }

    private void msgOnWrongRequest() {
        widgetViewWeather.setText(getText(R.string.error_wrong_request));
    }
}