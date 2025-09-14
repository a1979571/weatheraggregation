package assignment2;

import java.util.HashMap;
import java.util.Map;

public class WeatherEntry {

    private String id;
    private String name;
    private String state;
    private String timeZone;
    private String localDateTime;
    private String localDateTimeFull;
    private String cloud;
    private String windDir;

    private double lat;
    private double lon;
    private double airTemp;
    private double apparentTemp;
    private double dewpt;
    private double press;

    private int relHum;
    private int windSpdKmh;
    private int windSpdKt;

    private long lamportTime;
    private long lastUpdatedEpoch;

    // ===== Getters and Setters =====
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getTimeZone() { return timeZone; }
    public void setTimeZone(String timeZone) { this.timeZone = timeZone; }

    public String getLocalDateTime() { return localDateTime; }
    public void setLocalDateTime(String localDateTime) { this.localDateTime = localDateTime; }

    public String getLocalDateTimeFull() { return localDateTimeFull; }
    public void setLocalDateTimeFull(String localDateTimeFull) { this.localDateTimeFull = localDateTimeFull; }

    public String getCloud() { return cloud; }
    public void setCloud(String cloud) { this.cloud = cloud; }

    public String getWindDir() { return windDir; }
    public void setWindDir(String windDir) { this.windDir = windDir; }

    public double getLat() { return lat; }
    public void setLat(double lat) { this.lat = lat; }

    public double getLon() { return lon; }
    public void setLon(double lon) { this.lon = lon; }

    public double getAirTemp() { return airTemp; }
    public void setAirTemp(double airTemp) { this.airTemp = airTemp; }

    public double getApparentTemp() { return apparentTemp; }
    public void setApparentTemp(double apparentTemp) { this.apparentTemp = apparentTemp; }

    public double getDewpt() { return dewpt; }
    public void setDewpt(double dewpt) { this.dewpt = dewpt; }

    public double getPress() { return press; }
    public void setPress(double press) { this.press = press; }

    public int getRelHum() { return relHum; }
    public void setRelHum(int relHum) { this.relHum = relHum; }

    public int getWindSpdKmh() { return windSpdKmh; }
    public void setWindSpdKmh(int windSpdKmh) { this.windSpdKmh = windSpdKmh; }

    public int getWindSpdKt() { return windSpdKt; }
    public void setWindSpdKt(int windSpdKt) { this.windSpdKt = windSpdKt; }

    public long getLamportTime() { return lamportTime; }
    public void setLamportTime(long lamportTime) { this.lamportTime = lamportTime; }

    public long getLastUpdatedEpoch() { return lastUpdatedEpoch; }
    public void setLastUpdatedEpoch(long lastUpdatedEpoch) { this.lastUpdatedEpoch = lastUpdatedEpoch; }
    public void refreshLastUpdated() { this.lastUpdatedEpoch = System.currentTimeMillis(); }

    // ===== Serialization =====
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id != null ? id : "");
        map.put("name", name != null ? name : "");
        map.put("state", state != null ? state : "");
        map.put("lat", lat);
        map.put("lon", lon);
        map.put("air_temp", airTemp);
        map.put("apparent_t", apparentTemp);
        map.put("cloud", cloud != null ? cloud : "");
        map.put("dewpt", dewpt);
        map.put("press", press);
        map.put("rel_hum", relHum);
        map.put("wind_dir", windDir != null ? windDir : "");
        map.put("wind_spd_kmh", windSpdKmh);
        map.put("wind_spd_kt", windSpdKt);
        map.put("lamportTime", lamportTime);
        return map;
    }

    // ===== Deserialization =====
    public static WeatherEntry fromMap(Map<String, String> map) {
        WeatherEntry e = new WeatherEntry();
        e.setId(map.get("id")); // Must be non-null for aggregation
        e.setName(map.getOrDefault("name", ""));
        e.setState(map.getOrDefault("state", ""));
        e.setLat(parseDoubleSafe(map.get("lat")));
        e.setLon(parseDoubleSafe(map.get("lon")));
        e.setAirTemp(parseDoubleSafe(map.get("air_temp")));
        e.setApparentTemp(parseDoubleSafe(map.get("apparent_t")));
        e.setCloud(map.getOrDefault("cloud", ""));
        e.setDewpt(parseDoubleSafe(map.get("dewpt")));
        e.setPress(parseDoubleSafe(map.get("press")));
        e.setRelHum(parseIntSafe(map.get("rel_hum")));
        e.setWindDir(map.getOrDefault("wind_dir", ""));
        e.setWindSpdKmh(parseIntSafe(map.get("wind_spd_kmh")));
        e.setWindSpdKt(parseIntSafe(map.get("wind_spd_kt")));
        e.setLamportTime(parseLongSafe(map.get("lamportTime")));
        e.refreshLastUpdated();
        return e;
    }

    // ===== Helper parsers =====
    private static double parseDoubleSafe(String s) {
        try { return Double.parseDouble(s); }
        catch (Exception ignored) { return 0; }
    }

    private static int parseIntSafe(String s) {
        try { return Integer.parseInt(s); }
        catch (Exception ignored) { return 0; }
    }

    private static long parseLongSafe(String s) {
        try { return Long.parseLong(s); }
        catch (Exception ignored) { return 0L; }
    }
}
