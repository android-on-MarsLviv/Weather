package com.example.weather;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.JsonReader;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {

    private TextView msgTemperature;
    private EditText msgCity;
    private Button buttonByCity;
    private Button buttonByLocation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        msgTemperature = findViewById(R.id.msg_temperature);
        msgCity = findViewById(R.id.msg_city);
        buttonByCity = findViewById(R.id.button_by_city);
        buttonByLocation = findViewById(R.id.button_by_location);
    }

    public void onClickByCity(View view) {
        String city = msgCity.getText().toString();
        if (!checkCityString(city)) {
            msgOnWrongCity();
            return;
        }

        String request = formatRequestWithCity(city);
        Log.d("myTag", request);
        String response = doRequest(request);
        if (response != null) {
            Log.d("myTag", "len:" + response.length());
        } else {
            msgOnWrongRequest();
            return;
        }

        String formattedOutput = getTemperature(response);
        if (formattedOutput == null) {
            msgOnWrongCity();
            return;
        }
        String newMessage = getText(R.string.default_message) + " " + formattedOutput + " \u00B0C";
        msgTemperature.setText(newMessage);
    }

    public void onClickByLocation(View view) {

    }

    private boolean checkCityString(String city) {
        if (city == null) {
            msgOnWrongCity();
            return false;
        }
        else if (city.length() < 2 || city.length() > 30) {
            msgOnWrongCity();
            return false;
        }
        // TODO: do some other checking of input string
        return true;
    }

    private String formatRequestWithCity(String city) {
        String api_url = getText(R.string.weather_entry_point).toString();
        String key = getText(R.string.weather_key).toString();
        return api_url + "?q=" + city + "&appid=" + key + "&units=metric";
    }

    private String getTemperature(String response) {
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

    private String doRequest(String request) {
        final String[] respond = {null};

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                BufferedReader reader = null;
                InputStream stream = null;
                HttpsURLConnection connection = null;
                try {
                    URL weatherEndpoint = new URL(request);
                    connection = (HttpsURLConnection) weatherEndpoint.openConnection();
                    connection.setRequestProperty("User-Agent", getText(R.string.app_name).toString());
                    connection.setRequestMethod("GET");
                    connection.setReadTimeout(3000);
                    connection.connect();

                    if (connection.getResponseCode() == 200) {
                        stream = connection.getInputStream();
                        reader = new BufferedReader(new InputStreamReader(stream));
                        StringBuilder buf = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            buf.append(line).append("\n");
                        }

                        respond[0] = buf.toString();

                    } else {
                        Log.d("myTag", "not 200");
                    }

                    if(reader != null) {
                        reader.close();
                    }

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return respond[0];
    }

    private void msgOnWrongCity() {
        msgTemperature.setText("You typed wrong City. Try againg, please.");
    }

    private void msgOnWrongRequest() {
        msgTemperature.setText("Internet request Error. Try later, please.");
    }
}