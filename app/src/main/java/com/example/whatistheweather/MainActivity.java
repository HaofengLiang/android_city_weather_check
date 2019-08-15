package com.example.whatistheweather;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.JsonReader;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;


public class MainActivity extends AppCompatActivity {
    private static final String apiFormat = "https://openweathermap.org/data/2.5/weather?q=%s&appid=b6907d289e10d714a6e88b30761fae22";
    private TextView resultText;
    private EditText editText;

    private class WeatherDownloadTask extends AsyncTask<String, Void, HashMap<String, String>> {

        @Override
        protected HashMap<String, String> doInBackground(String... urls) {
            HttpURLConnection connection = null;
            try {
                URL url = new URL(urls[0]);
                connection = (HttpURLConnection) url.openConnection();
                InputStream in = connection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);

                StringBuilder builder = new StringBuilder();
                int data = reader.read();
                while (data != -1) {
                    builder.append((char) data);
                    data = reader.read();
                }
                reader.close();
                connection.disconnect();
                return getWeatherInfo(builder.toString());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
            return null;
        }

        private HashMap<String, String> getWeatherInfo(String jsonString) throws JSONException {
            HashMap<String, String> weatherInfo = new HashMap<>();
            JSONObject jsonObject = new JSONObject(jsonString);
            JSONArray jsonArray = jsonObject.getJSONArray("weather");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject object = jsonArray.getJSONObject(i);
                weatherInfo.put(object.getString("main"), object.getString("description"));
            }
            JSONObject main = jsonObject.getJSONObject("main");
            for (Iterator<String> it = main.keys(); it.hasNext(); ) {
                String key = it.next();
                weatherInfo.put(key, main.getString(key));
            }
            return weatherInfo;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editText = findViewById(R.id.cityNameInput);
        resultText = findViewById(R.id.resultsTextView);

    }

    public void getWeather(View view) {
        WeatherDownloadTask task = new WeatherDownloadTask();
        String cityName = editText.getText().toString();
        try {
            HashMap<String, String> weatherInfo = task.execute(String.format(apiFormat, cityName)).get();
            StringBuilder builder = new StringBuilder();
            for (String key : weatherInfo.keySet()) {
                builder.append(String.format("%s : %s\n", key, weatherInfo.get(key)));
            }
            resultText.setText(builder.toString());
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
            resultText.setText("Hmmm... \nThere if no result for the city name: " + cityName);
        }

        InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        mgr.hideSoftInputFromWindow(editText.getWindowToken(), 0);
    }
}
