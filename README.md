# Interview Prep

Coding practice for software engineering interviews, iterated on after code review. The commit history for each problem shows the progression from a first working pass to an optimized solution.

## Layout

Problems are organized by `company/problem/`. Each problem folder holds the problem statement and the solution.

```
interview-prep/
└── spacex/
    └── telemetry/
        ├── SPACEX-Problem.md     # problem statement
        └── TelemetryWindow.java  # solution
```

## Problems

| Problem | Language | Topics | Status |
|---|---|---|---|
| [Telemetry Anomaly Window](spacex/telemetry/SPACEX-Problem.md) | Java | Sliding window, `HashMap` of `ArrayDeque`s, eviction, monotonic deque | Correct per-sensor windowing; O(1) min/max in progress |

### Telemetry Anomaly Window

Stream sensor readings `(timestamp_ms, sensor_id, value)` and answer min, max, and mean for any sensor over the last 5,000 ms.

**Approach so far**
- One `ArrayDeque` per sensor, kept in a `HashMap` keyed by a `TelemetryId` enum.
- Readings older than the window are evicted from the front of the deque on `ingest`, and lazily on `query` so a sensor that has gone quiet doesn't return stale data.
- The latest timestamp is tracked as a field instead of being found with a scan.

**Next step:** add a monotonic max-deque and min-deque per sensor plus a running sum, so that `query` is O(1) and `ingest` is amortized O(1).

## Running

Not run yet. The solution doesn't have a `main` method or test harness so far. Next up is adding a small driver that replays the example from the problem statement and checks the expected min, max, and mean.
