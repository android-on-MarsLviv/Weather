package com.example.weather;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

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
            msgTemperature.setText(getText(R.string.msg_wrong_city));
            return;
        }
        String request = formatRequestWithCity(city);
        doRequest(request);

        //msgTemperature.setText("OK");

    }
    
    public void onClickByLocation(View view) {

    }

    private boolean checkCityString(String city) {
        // TODO: do some minor checking of input string
        return true;
    }



    private String formatRequestWithCity(String city) {
        String api_url = getText(R.string.weather_entry_point).toString();
        String key = getText(R.string.weather_key).toString();
        String api_url_all = api_url + "?q=" + city + "&appid=" + key + "&units=metric";
        return api_url_all;
    }

    private void doRequest(String request) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                // All your networking logic
                // should be here
            }
        });
    }
}