import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class JsonUtils {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static WeatherEntry parseFeedFile(File file) throws IOException {
        Map<String, String> map = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                int idx = line.indexOf(':');
                if (idx <= 0) continue;
                map.put(line.substring(0, idx).trim(), line.substring(idx + 1).trim());
            }
        }

        if (!map.containsKey("id")) return null;

        WeatherEntry entry = new WeatherEntry();
        entry.setId(map.get("id"));
        entry.setName(map.get("name"));
        entry.setState(map.get("state"));
        entry.setTimeZone(map.get("time_zone"));
        entry.setLocalDateTime(map.get("local_date_time"));
        entry.setLocalDateTimeFull(map.get("local_date_time_full"));
        entry.setCloud(map.get("cloud"));
        entry.setWindDir(map.get("wind_dir"));

        try { entry.setLat(Double.parseDouble(map.getOrDefault("lat","0"))); } catch(Exception ignored){}
        try { entry.setLon(Double.parseDouble(map.getOrDefault("lon","0"))); } catch(Exception ignored){}
        try { entry.setAirTemp(Double.parseDouble(map.getOrDefault("air_temp","0"))); } catch(Exception ignored){}
        try { entry.setApparentTemp(Double.parseDouble(map.getOrDefault("apparent_t","0"))); } catch(Exception ignored){}
        try { entry.setDewpt(Double.parseDouble(map.getOrDefault("dewpt","0"))); } catch(Exception ignored){}
        try { entry.setPress(Double.parseDouble(map.getOrDefault("press","0"))); } catch(Exception ignored){}
        try { entry.setRelHum(Integer.parseInt(map.getOrDefault("rel_hum","0"))); } catch(Exception ignored){}
        try { entry.setWindSpdKmh(Integer.parseInt(map.getOrDefault("wind_spd_kmh","0"))); } catch(Exception ignored){}
        try { entry.setWindSpdKt(Integer.parseInt(map.getOrDefault("wind_spd_kt","0"))); } catch(Exception ignored){}

        return entry;
    }

    public static String toJson(Object obj) {
        return gson.toJson(obj);
    }

    public static WeatherEntry fromJson(String json) {
        return gson.fromJson(json, WeatherEntry.class);
    }
}
