package com.dreamstop.common.protocol;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utility class for converting Java objects to JSON and vice-versa using Google
 * Gson.
 * Shared between client and server modules.
 */
public final class JsonUtils {

    // Standard date-time format for exchanging LocalDateTime across the network
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    // Configured Gson instance with custom adapter for Java 8 LocalDateTime
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class,
                    (JsonSerializer<LocalDateTime>) (src, typeOfSrc,
                            context) -> new JsonPrimitive(src.format(DATE_TIME_FORMATTER)))
            .registerTypeAdapter(LocalDateTime.class,
                    (JsonDeserializer<LocalDateTime>) (json, typeOfT, context) -> LocalDateTime
                            .parse(json.getAsString(), DATE_TIME_FORMATTER))
            .create();

    private JsonUtils() {
        // Private constructor for utility class
    }

    public static Gson getGson() {
        return GSON;
    }

    // Converts any Java object to its JSON string representation
    public static String toJson(Object obj) {
        if (obj == null) {
            return null;
        }
        return GSON.toJson(obj);
    }

    // Parses JSON string into a specific Java class (e.g. UserDTO.class)
    public static <T> T fromJson(String json, Class<T> classOfT) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }
        return GSON.fromJson(json, classOfT);
    }

    // Parses JSON string into a generic collection (e.g. List<ItemDTO>) using
    // TypeToken
    public static <T> T fromJson(String json, java.lang.reflect.Type typeOfT) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }
        return GSON.fromJson(json, typeOfT);
    }
}
