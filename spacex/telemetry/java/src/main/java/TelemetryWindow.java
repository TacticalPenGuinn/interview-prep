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

    private static class SensorWindow {
        ArrayDeque<TelemetryRecord> records = new ArrayDeque<>();
        ArrayDeque<TelemetryRecord> maxDeque = new ArrayDeque<>();
        ArrayDeque<TelemetryRecord> minDeque = new ArrayDeque<>();
        double sum = 0.0;
    }

    public enum TelemetryId {
        LOX_P,
        LOX_T,
        CH4_P,
        CH4_T
    }


    private HashMap<TelemetryId, SensorWindow> telemetryMap = new HashMap<>();
    private int windowSize = 5000; // 5 seconds
    private long mostRecentTimestamp = 0;

    /**
     * Evicts old telemetry records that are outside the specified window.
     * 
     * @param telemetryId The ID of the telemetry to evict records for.
     */
    private void evictOldRecords(TelemetryId telemetryId) {
        SensorWindow sensorWindow = telemetryMap.get(telemetryId);
        if (sensorWindow != null) {
            ArrayDeque<TelemetryRecord> records = sensorWindow.records;
            while (!records.isEmpty() && records.peek().getTelemetryTimeStamp_ms() <= mostRecentTimestamp - windowSize) {
                TelemetryRecord old = records.poll();
                sensorWindow.sum -= old.getTelemetryValue();

                if (sensorWindow.maxDeque.peekFirst() == old) sensorWindow.maxDeque.pollFirst();
                if (sensorWindow.minDeque.peekFirst() == old) sensorWindow.minDeque.pollFirst();
            }
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
        SensorWindow sensorWindow = telemetryMap.computeIfAbsent(telemetryId, k -> new SensorWindow());
        TelemetryRecord record = new TelemetryRecord(telemetryTimeStamp_ms, telemetryId, telemetryValue);

        sensorWindow.records.addLast(record);
        sensorWindow.sum += telemetryValue;

        while (!sensorWindow.maxDeque.isEmpty() && sensorWindow.maxDeque.peekLast().getTelemetryValue() <= telemetryValue) {
            sensorWindow.maxDeque.pollLast();
        }
        sensorWindow.maxDeque.addLast(record);

        while (!sensorWindow.minDeque.isEmpty() && sensorWindow.minDeque.peekLast().getTelemetryValue() >= telemetryValue) {
            sensorWindow.minDeque.pollLast();
        }
        sensorWindow.minDeque.addLast(record);

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

        SensorWindow sensorWindow = telemetryMap.get(telemetryId);
        
        if (sensorWindow == null) {
            return null;
        }

        evictOldRecords(telemetryId);

        if (sensorWindow.records.isEmpty()) {
            return null;
        }


        final float min = sensorWindow.minDeque.peekFirst().getTelemetryValue();
        final float max = sensorWindow.maxDeque.peekFirst().getTelemetryValue();
        final double mean = sensorWindow.sum / sensorWindow.records.size();

        return String.format("Min: %.2f, Max: %.2f, Mean: %.2f", min, max, mean);
    }

}