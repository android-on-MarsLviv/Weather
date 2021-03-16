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

    public void onClickByCity(View view) throws MalformedURLException {
        Log.d("myTag", "OnClick start");

        String city = widgetEditCity.getText().toString();
        if (!checkCityString(city)) {
            msgOnWrongCity();
            return;
        }

        URL request = formatRequestWithCity(city);
        Log.d("myTag", String.valueOf(request));

        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d("myTag", "run started");
                String respond = null;
                try {
                    respond = doRequest(request);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (respond != null) {
                    Log.d("myTag", "len:" + respond.length());
                } else {
                    Log.d("myTag", "respond == null");
                    widgetViewWeather.post(new Runnable() {
                        @Override
                        public void run() {
                            widgetViewWeather.setText(getText(R.string.error_wrong_request));
                        }
                    });

                    return;
                }

                String formattedOutput = parseTemperature(respond);
                if (formattedOutput == null) {
                    Log.d("myTag", "formattedOutput == null");
                    widgetViewWeather.post(new Runnable() {
                        @Override
                        public void run() {
                            widgetViewWeather.setText(getText(R.string.error_wrong_city));
                        }
                    });

                    return;
                }

                String newMessage = getText(R.string.default_message) + " " + formattedOutput + " \u00B0C";
                Log.d("myTag", "newMessage" + newMessage);
                widgetViewWeather.post(new Runnable() {
                    @Override
                    public void run() {
                        widgetViewWeather.setText(newMessage);
                    }
                });

                Log.d("myTag", "run finished");
            }
        }).start();

        Log.d("myTag", "OnClick finish");
    }

    public void onClickByLocation(View view) {

    }

    private boolean checkCityString(String city) {
        if (city == null) {
            msgOnWrongCity();
            return false;
        }
        // TODO: do some other checking of input string
        return true;
    }

    private URL formatRequestWithCity(String city) throws MalformedURLException {
        Uri builtUri = Uri.parse(getText(R.string.weather_api_entry_point).toString())
                .buildUpon()
                .appendQueryParameter("q", city)
                .appendQueryParameter("appid", getText(R.string.weather_api_key).toString())
                .appendQueryParameter("units", "metric")
                .build();
        return new URL(builtUri.toString());
    }

    private String parseTemperature(String response) {
        String ret = null;

        try {
            JSONObject json = new JSONObject(response);
            JSONObject main  = json.getJSONObject("main");
            ret = main.getString("temp");
            Log.d("myTag", "temperature = " + ret);
        } catch (JSONException e) {
            msgOnWrongRequest();
            e.printStackTrace();
        }

        return ret;
    }

    private String doRequest(URL weatherEndpoint) throws IOException {
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
            } else {
                Log.d("myTag", "not 200");
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
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