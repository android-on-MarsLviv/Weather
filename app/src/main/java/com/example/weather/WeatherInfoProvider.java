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
    private final String TAG = WeatherInfoProvider.class.getSimpleName();

    private final int HTTP_REQUEST_TIMEOUT = 3000;
    private final String REQUEST_METHOD = "GET";

    private final String JSON_MAIN = "main";
    private final String JSON_WIND = "wind";
    private final String TEMPERATURE = "temp";
    private final String HUMIDITY = "humidity";
    private final String VISIBILITY = "visibility";
    private final String WIND_SPEED = "speed";

    private final WeatherRequest weatherRequest;
    private Uri uri = null;

    public WeatherInfoProvider(@NonNull WeatherRequest weatherRequest) {
        this.weatherRequest = weatherRequest;
    }

    public void provideWeather(@NonNull WeatherInfoProvider.RequestCallback callback) {
        try {
            uri = weatherRequest.createRequestUri();
            Log.i(TAG, "provideWeather by uri:" + uri);
            String response = doRequest();
            if (response == null) {
                Log.i(TAG, "response is empty");
                callback.onRequestFailed();
                return;
            }
            Optional<WeatherInfo> weatherInfo = parseWeather(response);
            if (!weatherInfo.isPresent()) {
                Log.i(TAG, "weatherInfo is empty");
                callback.onRequestFailed();
                return;
            }
            WeatherInfo weather = weatherInfo.get();
            callback.onRequestSucceed(weather);
        } catch (IOException e) {
            e.printStackTrace();
            callback.onRequestFailed();
        }
    }

    @Nullable
    private String doRequest() throws IOException {
        Log.i(TAG, "doRequest");

        InputStream stream = null;
        HttpsURLConnection connection = null;

        try {
            connection = (HttpsURLConnection) new URL(uri.toString()).openConnection();
            connection.setRequestMethod(REQUEST_METHOD);
            connection.setReadTimeout(HTTP_REQUEST_TIMEOUT);
            connection.connect();

            if (connection.getResponseCode() == HttpsURLConnection.HTTP_OK) {
                stream = connection.getInputStream();
                return StreamUtils.streamToString(stream);
            } else {
                Log.i(TAG, "Error. Response code:" + connection.getResponseCode());
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
