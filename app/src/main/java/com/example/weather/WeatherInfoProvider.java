package com.example.weather;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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

    private final WeatherRequest weatherRequest;
    private Uri uri = null;

    public WeatherInfoProvider(@NonNull WeatherRequest weatherRequest) {
        this.weatherRequest = weatherRequest;
    }

    public void provideWeather(@NonNull WeatherInfoProvider.RequestCallback callback) {

        try {
            uri = weatherRequest.createRequestUri();
            String response = doRequest();
            if (response == null) {
                Log.d(TAG, "response is empty");
                callback.onRequestFailed();
                return;
            }
            Optional<WeatherInfo> weatherInfo = parseWeather(response);
            if (!weatherInfo.isPresent()) {
                Log.d(TAG, "weatherInfo is empty");
                callback.onRequestFailed();
                return;
            }
            WeatherInfo weather = weatherInfo.get();
            callback.onRequestSucceed(weather);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Nullable
    private String doRequest() throws IOException {
        Log.d(TAG, "doRequest start");

        InputStream stream = null;
        HttpsURLConnection connection = null;

        try {
            connection = (HttpsURLConnection) new URL(uri.toString()).openConnection();
            connection.setRequestMethod("GET");
            connection.setReadTimeout(HTTP_REQUEST_TIMEOUT);
            connection.connect();

            if (connection.getResponseCode() == HttpsURLConnection.HTTP_OK) {
                stream = connection.getInputStream();
                return StreamUtils.streamToString(stream);
            } else {
                return null;
            }
        } finally {
            StreamUtils.closeAll(stream);
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private Optional<WeatherInfo> parseWeather(@NonNull String response) {
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
        void onRequestSucceed(@NonNull WeatherInfo weatherInfo);
        void onRequestFailed();
    }
}
