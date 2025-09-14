package assignment2;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.StringJoiner;

public class JsonParser {

    // Converts a WeatherEntry to JSON string
    public static String toJson(WeatherEntry entry) {
        return mapToJson(entry.toMap());
    }

    // Converts a List of WeatherEntry objects to JSON array string
    public static String toJsonArray(List<WeatherEntry> entries) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < entries.size(); i++) {
            sb.append(toJson(entries.get(i)));
            if (i < entries.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    // Converts a Map<String, Object> to JSON string
    public static String mapToJson(Map<String, Object> map) {
        StringJoiner joiner = new StringJoiner(",", "{", "}");
        for (Map.Entry<String, Object> e : map.entrySet()) {
            String key = e.getKey();
            Object value = e.getValue();
            String valueStr;
            if (value instanceof Number || value instanceof Boolean) {
                valueStr = value.toString();
            } else {
                valueStr = "\"" + escapeJson(value.toString()) + "\"";
            }
            joiner.add("\"" + key + "\":" + valueStr);
        }
        return joiner.toString();
    }

    // Converts a JSON array string to List of Map<String,String>
    public static List<Map<String, String>> parseArray(String json) {
        List<Map<String, String>> list = new ArrayList<>();
        json = json.trim();
        if (json.startsWith("[")) json = json.substring(1);
        if (json.endsWith("]")) json = json.substring(0, json.length() - 1);
        String[] objects = json.split("\\},\\{");
        for (int i = 0; i < objects.length; i++) {
            String obj = objects[i];
            if (!obj.startsWith("{")) obj = "{" + obj;
            if (!obj.endsWith("}")) obj = obj + "}";
            list.add(parseObject(obj));
        }
        return list;
    }

    // Converts a JSON object string to Map<String,String>
    public static Map<String, String> parseObject(String json) {
        Map<String, String> map = new HashMap<>();
        json = json.trim();
        if (json.startsWith("{")) json = json.substring(1);
        if (json.endsWith("}")) json = json.substring(0, json.length() - 1);
        String[] pairs = json.split(",");
        for (String pair : pairs) {
            int idx = pair.indexOf(":");
            if (idx < 0) continue;
            String key = pair.substring(0, idx).trim().replaceAll("^\"|\"$", "");
            String value = pair.substring(idx + 1).trim().replaceAll("^\"|\"$", "");
            map.put(key, value);
        }
        return map;
    }

    // Escape quotes and backslashes in strings
    private static String escapeJson(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
