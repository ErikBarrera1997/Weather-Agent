package MariSoftware_Wheater_Services;

import Marisoftware_Wheater_Data.Location;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class WeatherService {
    private static final String API_KEY = "18ed03220e4c2cd1057ada45338b4ace";
    private static final String BASE_URL = "https://api.openweathermap.org/data/2.5/weather";

    public static Location getWeather(String city, String countryCode) throws IOException {
        String query = String.format("%s,%s", city, countryCode);
        String urlStr = String.format("%s?q=%s&units=metric&appid=%s", BASE_URL, URLEncoder.encode(query, StandardCharsets.UTF_8), API_KEY);

        HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
        conn.setRequestMethod("GET");

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            JsonObject json = new Gson().fromJson(response.toString(), JsonObject.class);

            String location = json.getAsJsonArray("weather")
                    .get(0).getAsJsonObject()
                    .get("main").getAsString();

            double temperature = json.getAsJsonObject("main")
                    .get("temp").getAsDouble();

            return new Location(location, temperature);
        }
    }

}
