import java.math.BigDecimal;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public class TelemetryWindow {
    
    public final class TelemetryRecord {
        private long telemetryTimeStamp_ms;
        private String telemetryId;
        private float telemetryValue;

        public TelemetryRecord(long telemetryTimeStamp_ms, String telemetryId, float telemetryValue) {
            this.telemetryTimeStamp_ms = telemetryTimeStamp_ms;
            this.telemetryId = telemetryId;
            this.telemetryValue = telemetryValue;
        }

        public long getTelemetryTimeStamp_ms() {
            return telemetryTimeStamp_ms;
        }

        public String getTelemetryId() {
            return telemetryId;
        }

        public float getTelemetryValue() {
            return telemetryValue;
        }

    } 

    public enum TelemetryId {
        LOX_P,
        LOX_T,
        CH4_P,
        CH4_T
    }


    private HashMap<String, ArrayDeque<TelemetryRecord>> telemetryMap = new HashMap<>();
    private int windowSize = 5000; // 5 seconds
    private long mostRecentTimestamp = 0;

    /**
     * Ingests a telemetry record into the telemetry window.
     * @param telemetryTimeStamp_ms
     * @param telemetryId
     * @param telemetryValue
     */
    public void ingest(long telemetryTimeStamp_ms, TelemetryId telemetryId, float telemetryValue) {
        mostRecentTimestamp = telemetryTimeStamp_ms;
        telemetryMap.computeIfAbsent(telemetryId.name(), k -> new ArrayDeque<>()).add(new TelemetryRecord(telemetryTimeStamp_ms, telemetryId.name(), telemetryValue));

        // Remove records that are outside the 5-second window
        ArrayDeque<TelemetryRecord> sensorRecords = telemetryMap.get(telemetryId.name());
        while (!sensorRecords.isEmpty() && sensorRecords.peek().getTelemetryTimeStamp_ms() < mostRecentTimestamp - windowSize) {
            sensorRecords.poll();
        }
    }

    /**
     * Returns the min, max, and mean of that sensor's readings from the last 5,000 ms, measured back from the most recent timestamp ingested across all sensors.
     *
     * @param telemetryId The telemetry ID to query.
     * @return String representing the min, max, and mean values.
     */
    public String query(TelemetryId telemetryId) {
        return query(telemetryId.name());
    }

    private String query(String telemetryId) {

        ArrayDeque<TelemetryRecord> sensorRecords = telemetryMap.get(telemetryId);
        
        if (sensorRecords == null) {
            return "None";
        }

        if (sensorRecords.isEmpty()) {
            return "None";
        }

        final float min = sensorRecords.stream().map(TelemetryRecord::getTelemetryValue).min(Float::compare).orElse(Float.NaN);
        final float max = sensorRecords.stream().map(TelemetryRecord::getTelemetryValue).max(Float::compare).orElse(Float.NaN);
        final float mean = sensorRecords.stream().map(TelemetryRecord::getTelemetryValue).reduce(0.0f, Float::sum) / sensorRecords.size();

        return String.format("Min: %.2f, Max: %.2f, Mean: %.2f", min, max, mean);
    }

}