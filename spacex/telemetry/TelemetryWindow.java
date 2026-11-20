import java.util.ArrayDeque;
import java.util.HashMap;

public class TelemetryWindow {
    
    public static class TelemetryRecord {
        private long telemetryTimeStamp_ms;
        private TelemetryId telemetryId;
        private float telemetryValue;

        public TelemetryRecord(long telemetryTimeStamp_ms, TelemetryId telemetryId, float telemetryValue) {
            this.telemetryTimeStamp_ms = telemetryTimeStamp_ms;
            this.telemetryId = telemetryId;
            this.telemetryValue = telemetryValue;
        }

        public long getTelemetryTimeStamp_ms() {
            return telemetryTimeStamp_ms;
        }

        public TelemetryId getTelemetryId() {
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


    private HashMap<TelemetryId, ArrayDeque<TelemetryRecord>> telemetryMap = new HashMap<>();
    private int windowSize = 5000; // 5 seconds
    private long mostRecentTimestamp = 0;

    /**
     * Evicts old telemetry records that are outside the specified window.
     * 
     * @param telemetryMap The map containing telemetry records.
     * @param mostRecentTimestamp The most recent timestamp.
     * @param windowSize The size of the window in milliseconds.
     * @param telemetryId The ID of the telemetry to evict records for.
     */
    private void evictOldRecords(TelemetryId telemetryId) {
        ArrayDeque<TelemetryRecord> sensorRecords = telemetryMap.get(telemetryId);
        while (!sensorRecords.isEmpty() && sensorRecords.peek().getTelemetryTimeStamp_ms() < mostRecentTimestamp - windowSize) {
            sensorRecords.poll();
        }
    }

    /**
     * Ingests a telemetry record into the telemetry window.
     * @param telemetryTimeStamp_ms
     * @param telemetryId
     * @param telemetryValue
     */
    public void ingest(long telemetryTimeStamp_ms, TelemetryId telemetryId, float telemetryValue) {
        mostRecentTimestamp = telemetryTimeStamp_ms;
        telemetryMap.computeIfAbsent(telemetryId, k -> new ArrayDeque<>()).add(new TelemetryRecord(telemetryTimeStamp_ms, telemetryId, telemetryValue));

        // Remove records that are outside the 5-second window
        evictOldRecords(telemetryId);
    }

    /**
     * Returns the min, max, and mean of that sensor's readings from the last 5,000 ms, measured back from the most recent timestamp ingested across all sensors.
     *
     * @param telemetryId The telemetry ID to query.
     * @return String representing the min, max, and mean values.
     */
    public String query(TelemetryId telemetryId) {

        ArrayDeque<TelemetryRecord> sensorRecords = telemetryMap.get(telemetryId);
        
        if (sensorRecords == null) {
            return null;
        }

        evictOldRecords(telemetryId);

        if (sensorRecords.isEmpty()) {
            return null;
        }


        final float min = sensorRecords.stream().map(TelemetryRecord::getTelemetryValue).min(Float::compare).orElse(Float.NaN);
        final float max = sensorRecords.stream().map(TelemetryRecord::getTelemetryValue).max(Float::compare).orElse(Float.NaN);
        final float mean = sensorRecords.stream().map(TelemetryRecord::getTelemetryValue).reduce(0.0f, Float::sum) / sensorRecords.size();

        return String.format("Min: %.2f, Max: %.2f, Mean: %.2f", min, max, mean);
    }

}