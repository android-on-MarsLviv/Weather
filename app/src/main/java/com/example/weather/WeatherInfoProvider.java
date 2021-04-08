package com.example.weather;

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

public class WeatherInfoProvider {
    private static final String TAG = WeatherInfoProvider.class.getSimpleName();
    private static final int HTTP_REQUEST_TIMEOUT = 3000;

    private static final String JSON_MAIN = "main";
    private static final String JSON_WIND = "wind";
    private static final String TEMPERATURE = "temp";
    private static final String HUMIDITY = "humidity";
    private static final String VISIBILITY = "visibility";
    private static final String WIND_SPEED = "speed";

    private final WeatherRequest weatherRequestData;

    public WeatherInfoProvider(@NonNull WeatherRequest weatherRequestData) {
        this.weatherRequestData = weatherRequestData;
    }

    public URL buildRequestUrlByCity() throws MalformedURLException {
        Uri builtUri = Uri.parse(weatherRequestData.getWeatherApiEntryPoint())
                .buildUpon()
                .appendQueryParameter("q", weatherRequestData.getCityName())
                .appendQueryParameter("appid", weatherRequestData.getWeatherApiKey())
                .appendQueryParameter("units", "metric")
                .build();
        return new URL(builtUri.toString());
    }

    public URL buildRequestUrlByLocation() throws MalformedURLException {
        Uri builtUri = Uri.parse(weatherRequestData.getWeatherApiEntryPoint())
                .buildUpon()
                .appendQueryParameter("lat", String.valueOf(weatherRequestData.getLocation().getLatitude()))
                .appendQueryParameter("lon", String.valueOf(weatherRequestData.getLocation().getLongitude()))
                .appendQueryParameter("appid", weatherRequestData.getWeatherApiKey())
                .appendQueryParameter("units", "metric")
                .build();
        return new URL(builtUri.toString());
    }

    public void doRequest(@NonNull URL weatherEndpoint, @NonNull WeatherInfoProvider.RequestCallback callback) throws IOException {
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

    public Optional<WeatherInfo> parseWeather(@NonNull String response) {
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
