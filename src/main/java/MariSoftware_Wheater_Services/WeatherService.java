package MariSoftware_Wheater_Services;

import Marisoftware_Wheater_Data.Location;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.github.cdimascio.dotenv.Dotenv;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class WeatherService {
    private static final Dotenv dotenv = Dotenv.configure().load();
    private static final String API_KEY = dotenv.get("WEATHER_API_KEY");
    private static final String BASE_URL = "https://api.openweathermap.org/data/2.5/weather";
    private static final String UNITS_PARAM = "metric";
    private static final int TIMEOUT_MS = 5000;
    private static final Gson GSON = new Gson();
    private static final Object CACHE_LOCK = new Object();
    private static CacheEntry cacheEntry;

    public static Location getWeather(String city, String countryCode) throws IOException {
        String query = String.format("%s,%s", city, countryCode);
        String urlStr = String.format("%s?q=%s&units=%s&appid=%s", BASE_URL, URLEncoder.encode(query, StandardCharsets.UTF_8), UNITS_PARAM, API_KEY);

        HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(TIMEOUT_MS);
        conn.setReadTimeout(TIMEOUT_MS);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            JsonObject json = GSON.fromJson(response.toString(), JsonObject.class);

            String location = json.getAsJsonArray("weather")
                    .get(0).getAsJsonObject()
                    .get("main").getAsString();

            double temperature = json.getAsJsonObject("main")
                    .get("temp").getAsDouble();

            return new Location(location, temperature);
        }
    }

    public static Location getWeatherCached(String city, String countryCode, long ttlMillis, boolean forceRefresh) throws IOException {
        if (!forceRefresh) {
            CacheEntry cached = getValidCache(city, countryCode, ttlMillis);
            if (cached != null) {
                return cached.location;
            }
        }

        Location fresh = getWeather(city, countryCode);
        synchronized (CACHE_LOCK) {
            cacheEntry = new CacheEntry(city, countryCode, fresh, System.currentTimeMillis());
        }
        return fresh;
    }

    private static CacheEntry getValidCache(String city, String countryCode, long ttlMillis) {
        synchronized (CACHE_LOCK) {
            if (cacheEntry == null) {
                return null;
            }
            if (!cacheEntry.city.equalsIgnoreCase(city) || !cacheEntry.countryCode.equalsIgnoreCase(countryCode)) {
                return null;
            }
            long age = System.currentTimeMillis() - cacheEntry.timestamp;
            return age <= ttlMillis ? cacheEntry : null;
        }
    }

    private static class CacheEntry {
        private final String city;
        private final String countryCode;
        private final Location location;
        private final long timestamp;

        private CacheEntry(String city, String countryCode, Location location, long timestamp) {
            this.city = city;
            this.countryCode = countryCode;
            this.location = location;
            this.timestamp = timestamp;
        }
    }
}
