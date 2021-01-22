package com.lyapunov.cyclingtracker.networking;
import android.util.Log;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class AddressCaller {

    private static AddressCaller addressCaller = new AddressCaller();
    private AddressCaller(){};
    public static AddressCaller getAddressCaller() {
        return addressCaller;
    }

    private static OkHttpClient client = new OkHttpClient();

    public String fetchCityData(double lat, double lng) throws IOException {
        StringBuffer buffer = new StringBuffer();
        buffer.append("https://maps.googleapis.com/maps/api/geocode/json?latlng=")
                .append(lat)
                .append(",")
                .append(lng)
                .append("&result_type=locality&key=")
                .append(Keys.getGeocoderKey());
        String url = buffer.toString();
        String result = run(url);
        if (result != null && result.length() != 0) {
            for (int i = 0; i < result.length() - 3; i++) {
                if (result.charAt(i) != 'U' && result.charAt(i + 1) != 'S' && result.charAt(i + 2) != 'A') {
                    continue;
                }
                int j = i + 2;
                while (j > 0) {
                    if (result.charAt(j) >= '0' && result.charAt(j) <= '9') {
                        break;
                    }
                    j--;
                }
                return result.substring(j + 2, i + 3);
            }
        }
        return "";
    }

    private String run(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();
        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        } catch (IOException e) {
            Log.e("IOException", String.valueOf(e));
            return "";
        }
    }
}



