package MariSoftware_Wheater_Services;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class LocationService {
    public static String[] getCityAndCountry() throws IOException {
        URL url = new URL("http://ip-api.com/json/");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            JsonObject json = new Gson().fromJson(response.toString(), JsonObject.class);
            String city = json.get("city").getAsString();
            String countryCode = json.get("countryCode").getAsString();

            return new String[]{city, countryCode};
        }
    }

}
