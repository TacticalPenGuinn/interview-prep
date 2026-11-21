# Interview Prep

[![tests](https://github.com/TacticalPenGuinn/interview-prep/actions/workflows/test.yml/badge.svg)](https://github.com/TacticalPenGuinn/interview-prep/actions/workflows/test.yml)

Coding practice for software engineering interviews, iterated on after code review. The commit history for each problem shows the progression from a first working pass to an optimized solution.

## Layout

Problems are organized by `company/problem/`, with one subfolder per language so the same problem can be solved in Java, Python, or C++.

```
interview-prep/
└── spacex/
    └── telemetry/
        ├── SPACEX-Problem.md                       # problem statement
        └── java/                                   # Gradle project
            ├── build.gradle.kts
            ├── src/main/java/TelemetryWindow.java  # solution
            └── src/test/java/TelemetryWindowTest.java
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

Each language folder builds on its own. For the Java solution (JDK 25):

```bash
cd spacex/telemetry/java
./gradlew test
```

GitHub Actions runs the same tests on every push. The test class is only a placeholder so far; real cases (the problem's example, a sensor that never reported, a sensor that went quiet, all-negative values, and the exact 5,000 ms boundary) are next.
