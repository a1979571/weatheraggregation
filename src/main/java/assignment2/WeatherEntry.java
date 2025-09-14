package assignment2;
import java.time.Instant;

public class WeatherEntry {
    private String id;
    private String name;
    private String state;
    private String timeZone;
    private double lat;
    private double lon;
    private String localDateTime;
    private String localDateTimeFull;
    private double airTemp;
    private double apparentTemp;
    private String cloud;
    private double dewpt;
    private double press;
    private int relHum;
    private String windDir;
    private int windSpdKmh;
    private int windSpdKt;
    private int lamportTime;
    private long lastUpdatedEpoch;

    public WeatherEntry() {
        this.lastUpdatedEpoch = 0; // initialized to 0 for deterministic tests
    }

    // Getters & setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public void setState(String state) { this.state = state; }
    public void setTimeZone(String timeZone) { this.timeZone = timeZone; }
    public double getLat() { return lat; }
    public void setLat(double lat) { this.lat = lat; }
    public void setLon(double lon) { this.lon = lon; }
    public void setLocalDateTime(String localDateTime) { this.localDateTime = localDateTime; }
    public void setLocalDateTimeFull(String localDateTimeFull) { this.localDateTimeFull = localDateTimeFull; }
    public double getAirTemp() { return airTemp; }
    public void setAirTemp(double airTemp) { this.airTemp = airTemp; }
    public void setApparentTemp(double apparentTemp) { this.apparentTemp = apparentTemp; }
    public void setCloud(String cloud) { this.cloud = cloud; }
    public void setDewpt(double dewpt) { this.dewpt = dewpt; }
    public void setPress(double press) { this.press = press; }
    public void setRelHum(int relHum) { this.relHum = relHum; }
    public void setWindDir(String windDir) { this.windDir = windDir; }
    public void setWindSpdKmh(int windSpdKmh) { this.windSpdKmh = windSpdKmh; }
    public void setWindSpdKt(int windSpdKt) { this.windSpdKt = windSpdKt; }
    public int getLamportTime() { return lamportTime; }
    public void setLamportTime(int lamportTime) { this.lamportTime = lamportTime; }
    public long getLastUpdatedEpoch() { return lastUpdatedEpoch; }
    public void setLastUpdatedEpoch(long epoch) { this.lastUpdatedEpoch = epoch; }
    public void refreshLastUpdated() { this.lastUpdatedEpoch = Instant.now().toEpochMilli(); }

    @Override
    public String toString() {
        return "WeatherEntry{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", state='" + state + '\'' +
                ", lamportTime=" + lamportTime +
                ", lastUpdated=" + lastUpdatedEpoch +
                '}';
    }
}