package com.dreamstop.common.protocol;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class JsonUtils {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, (JsonSerializer<LocalDateTime>) (src, typeOfSrc, context) ->
                    new JsonPrimitive(src.format(DATE_TIME_FORMATTER)))
            .registerTypeAdapter(LocalDateTime.class, (JsonDeserializer<LocalDateTime>) (json, typeOfT, context) ->
                    LocalDateTime.parse(json.getAsString(), DATE_TIME_FORMATTER))
            .create();

    private JsonUtils() {
    }

    public static Gson getGson() {
        return GSON;
    }

    public static String toJson(Object obj) {
        if (obj == null) {
            return null;
        }
        return GSON.toJson(obj);
    }

    public static <T> T fromJson(String json, Class<T> classOfT) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }
        return GSON.fromJson(json, classOfT);
    }
}
