package MariSoftware_Wheater_Services;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class LocationService {
    private static final String LOCATION_URL = "http://ip-api.com/json/";
    private static final int TIMEOUT_MS = 5000;
    private static final Gson GSON = new Gson();
    private static final Object CACHE_LOCK = new Object();
    private static CacheEntry cacheEntry;

    public static String[] getCityAndCountry() throws IOException {
        URL url = new URL(LOCATION_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
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
            String city = json.get("city").getAsString();
            String countryCode = json.get("countryCode").getAsString();

            return new String[]{city, countryCode};
        }
    }

    public static String[] getCityAndCountryCached(long ttlMillis, boolean forceRefresh) throws IOException {
        if (!forceRefresh) {
            CacheEntry cached = getValidCache(ttlMillis);
            if (cached != null) {
                return new String[]{cached.city, cached.countryCode};
            }
        }

        String[] fresh = getCityAndCountry();
        synchronized (CACHE_LOCK) {
            cacheEntry = new CacheEntry(fresh[0], fresh[1], System.currentTimeMillis());
        }
        return fresh;
    }

    private static CacheEntry getValidCache(long ttlMillis) {
        synchronized (CACHE_LOCK) {
            if (cacheEntry == null) {
                return null;
            }
            long age = System.currentTimeMillis() - cacheEntry.timestamp;
            return age <= ttlMillis ? cacheEntry : null;
        }
    }

    private static class CacheEntry {
        private final String city;
        private final String countryCode;
        private final long timestamp;

        private CacheEntry(String city, String countryCode, long timestamp) {
            this.city = city;
            this.countryCode = countryCode;
            this.timestamp = timestamp;
        }
    }
}
