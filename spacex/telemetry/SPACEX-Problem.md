Here's a SpaceX-flavored one at about LeetCode-medium level.

**Telemetry anomaly window**

A vehicle streams sensor readings as `(timestamp_ms, sensor_id, value)` tuples, sorted by timestamp. Write a class with two methods:

- `ingest(timestamp_ms, sensor_id, value)` adds a reading.
- `query(sensor_id)` returns the min, max, and mean of that sensor's readings from the last 5,000 ms, measured back from the most recent timestamp ingested across all sensors.

Requirements:
1. `ingest` should be amortized O(1), and `query` should be O(1) for min and max (a plain scan of the window doesn't count).
2. Readings older than the window must be evicted so memory doesn't grow forever.
3. A sensor with no readings in the window returns `None`.

Example:
```
ingest(1000, "LOX_P", 50.0)
ingest(3000, "LOX_P", 52.0)
ingest(4000, "CH4_T", -160.0)
ingest(7000, "LOX_P", 49.0)
query("LOX_P")  -> min 49.0, max 52.0, mean 50.5   (the 1000 ms reading has aged out)
query("CH4_T")  -> min -160.0, max -160.0, mean -160.0
```

**Hint, if you want one:** use monotonic deques for the min and max, plus a running sum and count per sensor.

**Stretch:** flag an anomaly whenever a new value is more than 3 standard deviations from the window mean, using O(1) state (look up Welford's algorithm, or keep a running sum of squares).

Paste your solution when you're done and I'll review it the way an interviewer would, covering correctness, edge cases, and complexity.