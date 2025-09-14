package assignment2;

/**
 * WeatherEntry
 *
 * Represents a weather observation.
 * Each entry includes a unique ID, location, condition, temperature,
 * timestamp of the entry, and the Lamport clock value.
 */
public class WeatherEntry {

    private String id;           // Unique identifier for the entry
    private String location;     // City or region
    private String condition;    // e.g., Sunny, Rainy
    private double temperature;  // in Celsius
    private long timestamp;      // System.currentTimeMillis() when entry is stored
    private int lamportTime;     // Lamport clock value

    // Default constructor needed for Gson
    public WeatherEntry() {}

    public WeatherEntry(String id, String location, String condition, double temperature, int lamportTime) {
        this.id = id;
        this.location = location;
        this.condition = condition;
        this.temperature = temperature;
        this.lamportTime = lamportTime;
        this.timestamp = System.currentTimeMillis();
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }

    public double getTemperature() { return temperature; }
    public void setTemperature(double temperature) { this.temperature = temperature; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public int getLamportTime() { return lamportTime; }
    public void setLamportTime(int lamportTime) { this.lamportTime = lamportTime; }

    @Override
    public String toString() {
        return "WeatherEntry{" +
                "id='" + id + '\'' +
                ", location='" + location + '\'' +
                ", condition='" + condition + '\'' +
                ", temperature=" + temperature +
                ", timestamp=" + timestamp +
                ", lamportTime=" + lamportTime +
                '}';
    }
}
