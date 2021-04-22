package com.example.weather;

import android.location.Location;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class WeatherRequest implements Parcelable {
    private static final String APP_ID = "appid";
    private static final String API_UNITS = "units";
    private static final String API_UNITS_VALUE = "metric";
    private static final String LATITUDE = "lat";
    private static final String LONGITUDE = "lon";
    private static final String CITY = "q";

    private String weatherApiKey;
    private String weatherApiEntryPoint;

    private String cityName;
    private Location location;

    WeatherRequest(@Nullable String cityName, @Nullable Location location, @NonNull String weatherApiKey, @NonNull String weatherApiEntryPoint) {
        this.cityName = cityName;
        this.weatherApiKey = weatherApiKey;
        this.weatherApiEntryPoint = weatherApiEntryPoint;
        this.location = location;
    }

    private WeatherRequest(Parcel parcel) {
        readFromParcel(parcel);
    }

    private void readFromParcel(Parcel parcel) {
        this.weatherApiKey = parcel.readString();
        this.weatherApiEntryPoint = parcel.readString();
        this.cityName = parcel.readString();
        this.location = parcel.readParcelable(Location.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(weatherApiKey);
        parcel.writeString(weatherApiEntryPoint);
        parcel.writeString(cityName);
        parcel.writeParcelable(location, flags);
    }

    public static final Parcelable.Creator<WeatherRequest> CREATOR = new Parcelable.Creator<WeatherRequest>(){
        @Override
        public WeatherRequest createFromParcel(Parcel parcel) {
            return new WeatherRequest(parcel);
        }

        @Override
        public WeatherRequest[] newArray(int size) {
            return new WeatherRequest[size];
        }
    };

    public Uri createRequestUri() {
        Uri.Builder uriBuilder = Uri.parse(weatherApiEntryPoint)
                .buildUpon()
                .appendQueryParameter(APP_ID, weatherApiKey)
                .appendQueryParameter(API_UNITS, API_UNITS_VALUE);

        if (cityName != null) {
            uriBuilder.appendQueryParameter(CITY, cityName);
        }

        if (location != null) {
            uriBuilder
                    .appendQueryParameter(LATITUDE, String.valueOf(location.getLatitude()))
                    .appendQueryParameter(LONGITUDE, String.valueOf(location.getLongitude()));
        }

        return uriBuilder.build();
    }

    public static class Builder {
        private final String weatherApiKey;
        private final String weatherApiEntryPoint;

        private String cityName;
        private Location location;

        public Builder(@NonNull String weatherApiKey, @NonNull String weatherApiEntryPoint) {
            this.weatherApiKey = weatherApiKey;
            this.weatherApiEntryPoint = weatherApiEntryPoint;
        }

        public Builder setCity(@NonNull String cityName) {
            this.cityName = cityName;

            return this;
        }

        public Builder setLocation(@NonNull Location location) {
            this.location = location;

            return this;
        }

        public WeatherRequest build() {
            return new WeatherRequest(cityName, location, weatherApiKey, weatherApiEntryPoint);
        }
    }
}
