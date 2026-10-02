package MariSoftware_Wheater_Services;

import Marisoftware_Wheater_Data.Location;
import Marisoftware_Wheater_Data.UserLocation;
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

    /**
     * Obtiene el clima usando las coordenadas lat/lon del usuario (más preciso que
     * buscar por nombre de ciudad). Si la API rechazara las coordenadas, reintenta
     * con la búsqueda clásica por "ciudad,pais".
     */
    public static Location getWeather(UserLocation userLocation) throws IOException {
        String coordUrl = String.format("%s?lat=%.4f&lon=%.4f&units=%s&appid=%s",
                BASE_URL, userLocation.getLatitude(), userLocation.getLongitude(), UNITS_PARAM, API_KEY);

        try {
            return fetchWeather(coordUrl);
        } catch (IOException coordError) {
            // Respaldo: búsqueda por nombre de ciudad y país.
            String query = String.format("%s,%s", userLocation.getCity(), userLocation.getCountryCode());
            String cityUrl = String.format("%s?q=%s&units=%s&appid=%s",
                    BASE_URL, URLEncoder.encode(query, StandardCharsets.UTF_8), UNITS_PARAM, API_KEY);
            try {
                return fetchWeather(cityUrl);
            } catch (IOException cityError) {
                cityError.addSuppressed(coordError);
                throw cityError;
            }
        }
    }

    private static Location fetchWeather(String urlStr) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(TIMEOUT_MS);
        conn.setReadTimeout(TIMEOUT_MS);

        try {
            int status = conn.getResponseCode();
            if (status < 200 || status >= 300) {
                throw new IOException("HTTP " + status + " al consultar el clima");
            }

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                JsonObject json = GSON.fromJson(response.toString(), JsonObject.class);
                if (json == null) {
                    throw new IOException("Respuesta JSON vacía o inválida del servicio de clima");
                }

                String location = json.getAsJsonArray("weather")
                        .get(0).getAsJsonObject()
                        .get("main").getAsString();

                double temperature = json.getAsJsonObject("main")
                        .get("temp").getAsDouble();

                return new Location(location, temperature);
            }
        } finally {
            conn.disconnect();
        }
    }

    public static Location getWeatherCached(UserLocation userLocation, long ttlMillis, boolean forceRefresh) throws IOException {
        if (!forceRefresh) {
            CacheEntry cached = getValidCache(userLocation, ttlMillis);
            if (cached != null) {
                return cached.location;
            }
        }

        Location fresh = getWeather(userLocation);
        synchronized (CACHE_LOCK) {
            cacheEntry = new CacheEntry(userLocation, fresh, System.currentTimeMillis());
        }
        return fresh;
    }

    private static CacheEntry getValidCache(UserLocation userLocation, long ttlMillis) {
        synchronized (CACHE_LOCK) {
            if (cacheEntry == null) {
                return null;
            }
            // La caché es válida solo para la misma ubicación (misma ciudad/país).
            if (!cacheEntry.userLocation.getCity().equalsIgnoreCase(userLocation.getCity())
                    || !cacheEntry.userLocation.getCountryCode().equalsIgnoreCase(userLocation.getCountryCode())) {
                return null;
            }
            long age = System.currentTimeMillis() - cacheEntry.timestamp;
            return age <= ttlMillis ? cacheEntry : null;
        }
    }

    private static class CacheEntry {
        private final UserLocation userLocation;
        private final Location location;
        private final long timestamp;

        private CacheEntry(UserLocation userLocation, Location location, long timestamp) {
            this.userLocation = userLocation;
            this.location = location;
            this.timestamp = timestamp;
        }
    }
}
