import java.math.BigDecimal;
import java.util.ArrayDeque;
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


    private ArrayDeque<TelemetryRecord> telemetryRecords = new ArrayDeque<>();

    /**
     * Ingests a telemetry record into the telemetry window.
     * @param telemetryTimeStamp_ms
     * @param telemetryId
     * @param telemetryValue
     */
    public void ingest(long telemetryTimeStamp_ms, String telemetryId, float telemetryValue) {
        telemetryRecords.add(new TelemetryRecord(telemetryTimeStamp_ms, telemetryId, telemetryValue));
    }

    /**
     * Returns the min, max, and mean of that sensor's readings from the last 5,000 ms, measured back from the most recent timestamp ingested across all sensors.
     *
     * @param telemetryId The telemetry ID to query.
     * @return String representing the min, max, and mean values.
     */
    public String query(String telemetryId) {

        AtomicReference<BigDecimal> min = new AtomicReference<>(new BigDecimal(Float.MAX_VALUE));
        AtomicReference<BigDecimal> max = new AtomicReference<>(new BigDecimal(Float.MIN_VALUE));
        AtomicLong sum = new AtomicLong(0);
        AtomicInteger count = new AtomicInteger(0);    

        long mostRecentTimestamp = telemetryRecords.stream()
                .mapToLong(TelemetryRecord::getTelemetryTimeStamp_ms)
                .max()
                .orElse(0);

        telemetryRecords.stream()
                .filter(record -> record.getTelemetryId().equals(telemetryId))
                .filter(record -> record.getTelemetryTimeStamp_ms() >= mostRecentTimestamp - 5000)
                .limit(5000)
                .forEach(record -> {
                    min.set(new BigDecimal(Math.min(min.get().floatValue(), record.getTelemetryValue())));
                    max.set(new BigDecimal(Math.max(max.get().floatValue(), record.getTelemetryValue())));
                    sum.getAndAdd((long) record.getTelemetryValue());
                    count.getAndIncrement();
                });

        return String.format("Telemetry ID: %s, Min: %.2f, Max: %.2f, Mean: %.2f", telemetryId, min.get().floatValue(), max.get().floatValue(), count.get() > 0 ? (float) sum.get() / count.get() : 0);
    }

}