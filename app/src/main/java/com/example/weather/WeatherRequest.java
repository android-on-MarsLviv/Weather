package com.example.weather;

import android.content.res.Resources;
import android.location.Location;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;

import javax.net.ssl.HttpsURLConnection;

public class WeatherRequest {
    private static final String TAG = WeatherRequest.class.getSimpleName();
    private static final int HTTP_REQUEST_TIMEOUT = 3000;

    private static final String JSON_MAIN = "main";
    private static final String JSON_WIND = "wind";
    private static final String TEMPERATURE = "temp";
    private static final String HUMIDITY = "humidity";
    private static final String VISIBILITY = "visibility";
    private static final String WIND_SPEED = "speed";

    Resources resources;

    WeatherRequest (Resources resources) {
        this.resources = resources;
    }

    public URL buildRequestUrl(String cityName) throws MalformedURLException {
        Uri builtUri = Uri.parse(resources.getText(R.string.weather_api_entry_point).toString())
                .buildUpon()
                .appendQueryParameter("q", cityName)
                .appendQueryParameter("appid", resources.getText(R.string.weather_api_key).toString())
                .appendQueryParameter("units", "metric")
                .build();
        return new URL(builtUri.toString());
    }

    public URL buildRequestUrl(Location location) throws MalformedURLException {
        Uri builtUri = Uri.parse(resources.getText(R.string.weather_api_entry_point).toString())
                .buildUpon()
                .appendQueryParameter("lat", String.valueOf(location.getLatitude()))
                .appendQueryParameter("lon", String.valueOf(location.getLongitude()))
                .appendQueryParameter("appid", resources.getText(R.string.weather_api_key).toString())
                .appendQueryParameter("units", "metric")
                .build();
        return new URL(builtUri.toString());
    }

    public void doRequest(@NonNull URL weatherEndpoint, @NonNull WeatherRequest.RequestCallback callback) throws IOException {
        Log.d(TAG, "doRequest start");

        InputStream stream = null;
        HttpsURLConnection connection = null;

        try {
            connection = (HttpsURLConnection) weatherEndpoint.openConnection();
            connection.setRequestMethod("GET");
            connection.setReadTimeout(HTTP_REQUEST_TIMEOUT);
            connection.connect();

            if (connection.getResponseCode() == HttpsURLConnection.HTTP_OK) {
                stream = connection.getInputStream();
                String respond = StreamUtils.streamToString(stream);

                callback.onRequestSucceed(respond);
            } else {
                callback.onRequestFailed();
            }
        } finally {
            StreamUtils.closeAll(stream);
            if (connection != null) {
                connection.disconnect();
            }
        }
        Log.d(TAG, "doRequest finish");
    }

    public Optional<WeatherInfo> parseWeather(String response) {
        try {
            JSONObject json = new JSONObject(response);
            JSONObject main = json.getJSONObject(JSON_MAIN);
            JSONObject wind = json.getJSONObject(JSON_WIND);
            return Optional.of(new WeatherInfo(
                    main.getString(TEMPERATURE),
                    main.getString(HUMIDITY),
                    json.getString(VISIBILITY),
                    wind.getString(WIND_SPEED)));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }

    public interface RequestCallback {
        void onRequestSucceed(@NonNull String respond);
        void onRequestFailed();
    }
}
