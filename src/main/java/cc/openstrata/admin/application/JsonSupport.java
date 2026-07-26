package cc.openstrata.admin.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;

/** Small helper for (de)serializing string-list / string-map columns stored as TEXT/JSON. */
public final class JsonSupport {
    private static final ObjectMapper M = new ObjectMapper();
    private static final TypeReference<List<String>> LIST = new TypeReference<>() {};
    private static final TypeReference<Map<String, String>> MAP = new TypeReference<>() {};

    private JsonSupport() {}

    public static String write(List<String> values) {
        try {
            return M.writeValueAsString(values == null ? List.of() : values);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize list column", e);
        }
    }

    public static List<String> read(String json) {
        if (json == null || json.isBlank()) return List.of();
        try {
            return M.readValue(json, LIST);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to deserialize list column", e);
        }
    }

    public static String writeMap(Map<String, String> values) {
        try {
            return M.writeValueAsString(values == null ? Map.of() : values);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize map column", e);
        }
    }

    public static Map<String, String> readMap(String json) {
        if (json == null || json.isBlank()) return Map.of();
        try {
            return M.readValue(json, MAP);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to deserialize map column", e);
        }
    }
}
