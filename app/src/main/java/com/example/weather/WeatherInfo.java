package com.example.weather;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class WeatherInfo implements Parcelable {
    private String temperature;
    private String humidity;
    private String visibility;
    private String windSpeed;

    WeatherInfo(@NonNull String temperature, @NonNull String humidity, @NonNull String visibility, @NonNull String windSpeed) {
        this.temperature = temperature;
        this.humidity = humidity;
        this.visibility = visibility;
        this.windSpeed = windSpeed;
    }

    private WeatherInfo(Parcel parcel) {
        readFromParcel(parcel);
    }

    public void readFromParcel(Parcel parcel) {
        this.temperature = parcel.readString();
        this.humidity = parcel.readString();
        this.visibility = parcel.readString();
        this.windSpeed = parcel.readString();
    }

    public String getTemperature() {
        return temperature;
    }

    public String getHumidity() {
        return humidity;
    }

    public String getVisibility() {
        return visibility;
    }

    public String getWindSpeed() { return windSpeed;  }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(temperature);
        parcel.writeString(humidity);
        parcel.writeString(visibility);
        parcel.writeString(windSpeed);
    }

    public static final Parcelable.Creator<WeatherInfo> CREATOR = new Parcelable.Creator<WeatherInfo>(){
        @Override
        public WeatherInfo createFromParcel(Parcel parcel) {
            return new WeatherInfo(parcel);
        }

        @Override
        public WeatherInfo[] newArray(int size) {
            return new WeatherInfo[size];
        }
    };
}
