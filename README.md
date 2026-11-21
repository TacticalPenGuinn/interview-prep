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
| [Telemetry Anomaly Window](spacex/telemetry/SPACEX-Problem.md) | Java | Sliding window, `HashMap` of `ArrayDeque`s, eviction, monotonic deque | Done: O(1) query, amortized O(1) ingest, JUnit tests |

### Telemetry Anomaly Window

Stream sensor readings `(timestamp_ms, sensor_id, value)` and answer min, max, and mean for any sensor over the last 5,000 ms.

**Approach**
- Each sensor gets a `SensorWindow` holding its readings in an `ArrayDeque`, plus a monotonic max deque, a monotonic min deque, and a running sum.
- On `ingest`, smaller older values are popped off the back of the max deque (larger ones off the min deque) before the new reading is added, so the front of each is always the current max or min.
- Readings that age out are evicted from the front on `ingest`, and lazily on `query` so a sensor that has gone quiet returns nothing. Evicting a reading also subtracts it from the sum and removes it from the front of either deque if it's there.
- `query` reads the two fronts and divides the sum by the count: O(1). Each reading enters and leaves each deque at most once, so `ingest` is amortized O(1).

**Next step:** solve it again in Python or C++, or try the stretch goal (3-sigma anomaly flagging with Welford's algorithm).

## Running

Each language folder builds on its own. For the Java solution (JDK 25):

```bash
cd spacex/telemetry/java
./gradlew test
```

GitHub Actions runs the same tests on every push. The JUnit suite covers the problem's example, per-sensor windowing with interleaved sensors, a sensor that never reported, a sensor that went quiet while another kept time moving, negative values, a max or min aging out while smaller or larger values remain, and the exact 5,000 ms boundary (a reading exactly 5,000 ms old has aged out).
