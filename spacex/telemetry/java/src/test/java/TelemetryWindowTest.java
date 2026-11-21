import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;


public class TelemetryWindowTest {
    
    private TelemetryWindow telemetryWindow;

    @BeforeEach 
    public void setup() {
        telemetryWindow = new TelemetryWindow();
    }

    @Test
    public void testEvictOldRecords() {
        telemetryWindow.ingest(0000L, TelemetryWindow.TelemetryId.LOX_P, 0.0f); // this record should be evicted
        telemetryWindow.ingest(1000L, TelemetryWindow.TelemetryId.LOX_P, 1.0f); // this record should be evicted
        telemetryWindow.ingest(2000L, TelemetryWindow.TelemetryId.LOX_P, 2.0f);
        telemetryWindow.ingest(3000L, TelemetryWindow.TelemetryId.LOX_P, 3.0f);
        telemetryWindow.ingest(4000L, TelemetryWindow.TelemetryId.LOX_P, 4.0f);
        telemetryWindow.ingest(6000L, TelemetryWindow.TelemetryId.LOX_P, 5.0f);

        String stats = telemetryWindow.query(TelemetryWindow.TelemetryId.LOX_P);
        assertEquals("Min: 2.00, Max: 5.00, Mean: 3.50", stats);
    }

    @Test 
    public void testQueryWithNoRecords() {
        String stats = telemetryWindow.query(TelemetryWindow.TelemetryId.LOX_P);
        assertNull(stats);
    }

    @Test
    public void testQueryWithAllRecordsEvicted() {
        // All six of these readings are evicted by the 11000 ms reading below (the 6000 one is exactly 5,000 ms old)
        telemetryWindow.ingest(0000L, TelemetryWindow.TelemetryId.LOX_P, 0.0f);
        telemetryWindow.ingest(1000L, TelemetryWindow.TelemetryId.LOX_P, 1.0f);
        telemetryWindow.ingest(2000L, TelemetryWindow.TelemetryId.LOX_P, 2.0f);
        telemetryWindow.ingest(3000L, TelemetryWindow.TelemetryId.LOX_P, 3.0f);
        telemetryWindow.ingest(4000L, TelemetryWindow.TelemetryId.LOX_P, 4.0f);
        telemetryWindow.ingest(6000L, TelemetryWindow.TelemetryId.LOX_P, 5.0f);

        // Ingest a new record that moves the window past every earlier reading
        telemetryWindow.ingest(11000L, TelemetryWindow.TelemetryId.LOX_P, 6.0f);

        String stats = telemetryWindow.query(TelemetryWindow.TelemetryId.LOX_P);
        assertEquals("Min: 6.00, Max: 6.00, Mean: 6.00", stats);
    }

    @Test 
    public void testSensorThatHasGoneQuiet() {
        telemetryWindow.ingest(1000L, TelemetryWindow.TelemetryId.LOX_P, 1.0f); // this record should be evicted

        // Ingest a new record for a different sensor that will evict all previous records for LOX_P
        telemetryWindow.ingest(7000L, TelemetryWindow.TelemetryId.CH4_P, 6.0f);

        String loxPStats = telemetryWindow.query(TelemetryWindow.TelemetryId.LOX_P);
        String ch4Stats = telemetryWindow.query(TelemetryWindow.TelemetryId.CH4_P);

        assertNull(loxPStats);
        assertEquals("Min: 6.00, Max: 6.00, Mean: 6.00", ch4Stats);
   
    }

    @Test void testQueryWithMultipleTelemetryIds() {
        // Readings arrive in timestamp order across sensors, so the two sensors are interleaved.
        telemetryWindow.ingest(0000L, TelemetryWindow.TelemetryId.LOX_P, 0.0f);  // evicted
        telemetryWindow.ingest(0000L, TelemetryWindow.TelemetryId.CH4_P, 10.0f); // evicted
        telemetryWindow.ingest(1000L, TelemetryWindow.TelemetryId.LOX_P, 1.0f);  // evicted (exactly 5,000 ms old)
        telemetryWindow.ingest(1000L, TelemetryWindow.TelemetryId.CH4_P, 11.0f); // evicted (exactly 5,000 ms old)
        telemetryWindow.ingest(2000L, TelemetryWindow.TelemetryId.LOX_P, 2.0f);
        telemetryWindow.ingest(2000L, TelemetryWindow.TelemetryId.CH4_P, 12.0f);
        telemetryWindow.ingest(3000L, TelemetryWindow.TelemetryId.LOX_P, 3.0f);
        telemetryWindow.ingest(3000L, TelemetryWindow.TelemetryId.CH4_P, 13.0f);
        telemetryWindow.ingest(4000L, TelemetryWindow.TelemetryId.LOX_P, 4.0f);
        telemetryWindow.ingest(4000L, TelemetryWindow.TelemetryId.CH4_P, 14.0f);
        telemetryWindow.ingest(6000L, TelemetryWindow.TelemetryId.LOX_P, 5.0f);
        telemetryWindow.ingest(6000L, TelemetryWindow.TelemetryId.CH4_P, 15.0f);

        String loxPStats = telemetryWindow.query(TelemetryWindow.TelemetryId.LOX_P);
        assertEquals("Min: 2.00, Max: 5.00, Mean: 3.50", loxPStats);

        String ch4PStats = telemetryWindow.query(TelemetryWindow.TelemetryId.CH4_P);
        assertEquals("Min: 12.00, Max: 15.00, Mean: 13.50", ch4PStats);
    }

    @Test
    public void testProblemExample() {
        telemetryWindow.ingest(1000L, TelemetryWindow.TelemetryId.LOX_P, 50.0f); // this record should be evicted
        telemetryWindow.ingest(3000L, TelemetryWindow.TelemetryId.LOX_P, 52.0f);
        telemetryWindow.ingest(4000L, TelemetryWindow.TelemetryId.CH4_P, -160.0f);
        telemetryWindow.ingest(7000L, TelemetryWindow.TelemetryId.LOX_P, 49.0f);

        String loxPStats = telemetryWindow.query(TelemetryWindow.TelemetryId.LOX_P);
        String ch4PStats = telemetryWindow.query(TelemetryWindow.TelemetryId.CH4_P);
        
        assertEquals("Min: 49.00, Max: 52.00, Mean: 50.50", loxPStats);
        assertEquals("Min: -160.00, Max: -160.00, Mean: -160.00", ch4PStats);
    }

}
