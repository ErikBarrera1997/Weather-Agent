package MariSoftware_Wheater_Services;

import Marisoftware_Wheater_Data.UserLocation;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Sistema de localización: obtiene ciudad, país y coordenadas (lat/lon) del usuario
 * a partir de su IP (geolocalización por IP) usando proveedores gratuitos,
 * con respaldo automático.
 */
public class LocationService {
    // Proveedor principal (gratuito, sin API key). Nota: ip-api.com solo funciona por HTTP.
    private static final String PRIMARY_URL = "http://ip-api.com/json/?fields=status,message,city,countryCode,lat,lon";
    // Proveedor de respaldo (HTTPS, gratuito, sin API key).
    private static final String FALLBACK_URL = "https://ipwho.is/";
    private static final int TIMEOUT_MS = 5000;
    private static final Gson GSON = new Gson();
    private static final Object CACHE_LOCK = new Object();
    private static CacheEntry cacheEntry;

    /**
     * Obtiene la ubicación del usuario (ciudad, código de país, lat/lon) consultando
     * el proveedor principal y, si este falla, el proveedor de respaldo.
     */
    public static UserLocation getLocation() throws IOException {
        try {
            return fetchFromPrimary();
        } catch (Exception primaryError) {
            try {
                return fetchFromFallback();
            } catch (Exception fallbackError) {
                IOException ex = new IOException(
                        "No se pudo obtener la ubicación (proveedores primario y de respaldo fallaron)",
                        primaryError);
                ex.addSuppressed(fallbackError);
                throw ex;
            }
        }
    }

    private static UserLocation fetchFromPrimary() throws IOException {
        JsonObject json = requestJson(PRIMARY_URL);

        // ip-api devuelve {"status":"fail","message":"..."} cuando no puede geolocalizar.
        if (!"success".equalsIgnoreCase(getString(json, "status"))) {
            throw new IOException("ip-api reporto fallo: " + getString(json, "message"));
        }

        String city = getString(json, "city");
        String countryCode = getString(json, "countryCode");
        double latitude = getDouble(json, "lat");
        double longitude = getDouble(json, "lon");
        validate(city, countryCode, latitude, longitude);
        return new UserLocation(city, countryCode, latitude, longitude);
    }

    private static UserLocation fetchFromFallback() throws IOException {
        JsonObject json = requestJson(FALLBACK_URL);

        // ipwho.is devuelve {"success":false,...} en caso de fallo.
        if (json.has("success") && !json.get("success").getAsBoolean()) {
            throw new IOException("ipwho.is reporto fallo: " + getString(json, "message"));
        }

        String city = getString(json, "city");
        String countryCode = getString(json, "country_code");
        double latitude = getDouble(json, "latitude");
        double longitude = getDouble(json, "longitude");
        validate(city, countryCode, latitude, longitude);
        return new UserLocation(city, countryCode, latitude, longitude);
    }

    private static JsonObject requestJson(String urlStr) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(TIMEOUT_MS);
        conn.setReadTimeout(TIMEOUT_MS);

        try {
            int status = conn.getResponseCode();
            if (status < 200 || status >= 300) {
                throw new IOException("HTTP " + status + " al consultar " + urlStr);
            }

            StringBuilder response = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
            }

            JsonObject json = GSON.fromJson(response.toString(), JsonObject.class);
            if (json == null) {
                throw new IOException("Respuesta JSON vacía o inválida de " + urlStr);
            }
            return json;
        } finally {
            conn.disconnect();
        }
    }

    private static String getString(JsonObject json, String key) {
        if (json.has(key) && !json.get(key).isJsonNull()) {
            return json.get(key).getAsString();
        }
        return null;
    }

    private static double getDouble(JsonObject json, String key) {
        if (json.has(key) && !json.get(key).isJsonNull()) {
            try {
                return Double.parseDouble(json.get(key).getAsString());
            } catch (NumberFormatException e) {
                return Double.NaN;
            }
        }
        return Double.NaN;
    }

    private static void validate(String city, String countryCode, double latitude, double longitude) throws IOException {
        if (city == null || city.isBlank() || countryCode == null || countryCode.isBlank()) {
            throw new IOException("La respuesta de geolocalización no contiene ciudad/país válidos");
        }
        // Coordenadas presentes y dentro de rangos válidos (-90..90 lat, -180..180 lon).
        if (Double.isNaN(latitude) || Double.isNaN(longitude)
                || latitude < -90 || latitude > 90
                || longitude < -180 || longitude > 180) {
            throw new IOException("La respuesta de geolocalización no contiene coordenadas lat/lon válidas");
        }
    }

    public static UserLocation getLocationCached(long ttlMillis, boolean forceRefresh) throws IOException {
        if (!forceRefresh) {
            CacheEntry cached = getValidCache(ttlMillis);
            if (cached != null) {
                return cached.location;
            }
        }

        UserLocation fresh = getLocation();
        synchronized (CACHE_LOCK) {
            cacheEntry = new CacheEntry(fresh, System.currentTimeMillis());
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
        private final UserLocation location;
        private final long timestamp;

        private CacheEntry(UserLocation location, long timestamp) {
            this.location = location;
            this.timestamp = timestamp;
        }
    }
}
