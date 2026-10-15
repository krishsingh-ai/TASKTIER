package com.tasktier.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for JSON serialization, deserialization, and HTTP response writing using Gson.
 */
public final class JsonUtil {

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, (JsonSerializer<LocalDateTime>) (src, typeOfSrc, context) ->
                    new JsonPrimitive(DateUtil.formatForDb(src)))
            .registerTypeAdapter(LocalDateTime.class, (JsonDeserializer<LocalDateTime>) (json, typeOfT, context) ->
                    DateUtil.parse(json.getAsString()))
            .setPrettyPrinting()
            .create();

    private JsonUtil() {
        // Prevent instantiation
    }

    /**
     * Serializes an object to JSON string.
     *
     * @param object object to serialize
     * @return JSON string
     */
    public static String toJson(Object object) {
        return GSON.toJson(object);
    }

    /**
     * Deserializes a JSON string into an object of the target class.
     *
     * @param json JSON content
     * @param classOfT target class type
     * @param <T> generic return type
     * @return parsed object
     */
    public static <T> T fromJson(String json, Class<T> classOfT) {
        return GSON.fromJson(json, classOfT);
    }

    /**
     * Writes an object as a JSON response with the given HTTP status code.
     *
     * @param response HTTP response
     * @param statusCode HTTP status
     * @param data response payload
     * @throws IOException on I/O error
     */
    public static void writeJsonResponse(HttpServletResponse response, int statusCode, Object data) throws IOException {
        response.setStatus(statusCode);
        response.setContentType("application/json;charset=UTF-8");
        try (PrintWriter writer = response.getWriter()) {
            writer.write(toJson(data));
            writer.flush();
        }
    }

    /**
     * Writes a structured JSON error response.
     *
     * @param response HTTP response
     * @param statusCode HTTP error status
     * @param message error message
     * @throws IOException on I/O error
     */
    public static void writeJsonError(HttpServletResponse response, int statusCode, String message) throws IOException {
        Map<String, Object> errorPayload = new HashMap<>();
        errorPayload.put("success", false);
        errorPayload.put("error", message);
        errorPayload.put("status", statusCode);
        writeJsonResponse(response, statusCode, errorPayload);
    }

    /**
     * Writes a structured JSON success response.
     *
     * @param response HTTP response
     * @param statusCode HTTP status (e.g. 200, 201)
     * @param message informative message
     * @param data optional payload
     * @throws IOException on I/O error
     */
    public static void writeJsonSuccess(HttpServletResponse response, int statusCode, String message, Object data) throws IOException {
        Map<String, Object> payload = new HashMap<>();
        payload.put("success", true);
        payload.put("message", message);
        if (data != null) {
            payload.put("data", data);
        }
        writeJsonResponse(response, statusCode, payload);
    }
}
