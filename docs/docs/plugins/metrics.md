---
sidebar_position: 5
sidebar_label: Collecting Metrics
title: Metrics Plugin
---

FlowMVI ships a metrics plugin that instruments the store (or plugin) pipeline and periodically
exports rich runtime statistics for intents, actions, states, subscriptions, lifecycle, and exception handling.

These metrics are designed to help you evaluate your stores' performance to find bottlenecks, slow loading,
excessive allocations or memory usage, excessive restarts, state updates, lock contention, subscription count,
excessive errors side effect count, as well as behavioral patterns from real-world users.

:::warning[Experimental]
This feature is experimental. Expect breaking changes in metrics schema.
:::

## Metrics collected

The schema is versioned via `MetricsSchemaVersion` and rendered through a `MetricSurface` for compatibility.
Each snapshot contains:

- **Intents** – totals, processed/dropped/undelivered counts, ops/sec, queue time, latency quantiles (p50/p90/p95/p99),
  inter-arrival times, bursts, buffer occupancy, plugin overhead.
- **Actions** – sent/delivered/undelivered counts, ops/sec, delivery latency quantiles, queue time, buffer metrics,
  plugin overhead.
- **State** – transition counts, vetoed transitions, reducer latency quantiles, throughput.
- **Subscriptions** – subscribe/unsubscribe events, current/peak subscribers, average/median lifetimes, sampled counts.
- **Lifecycle** – start/stop counters, total uptime, current/average/median lifetimes, bootstrap latency.
- **Exceptions** – total/handled counts, recovery latency (average/median).
- **Meta** – schema version, window length, EMA alpha, generated-at timestamp, start time, store name/id, run id.

Total: 66+ numeric metrics per snapshot.

## Usage guide

### 1. Add the dependency

```toml
flowmvi-metrics = { module = "pro.respawn.flowmvi:metrics", version.ref = "flowmvi" }
```

```kotlin
commonMainImplementation("pro.respawn.flowmvi:metrics:<version>")
```

The artifact is lightweight and depends only on kotlinx.serialization.

### 2. Install the decorator

To collect metrics, you need a decorator, a plugin, and a sink:

```kotlin
val store = store(Initial) {
    val metrics = collectMetrics(
        reportingScope = applicationScope
    )
    reportMetrics(
        metrics = metrics,
        interval = 10.seconds,
        sink = OtlpJsonSink(BackendSink()), // example
    )
}
```

:::warning[reporting scope must outlive the store]
**Use a long-lived `reportingScope` (application/process/component scope) so metrics survive store restarts.**
Any metrics are only updated/emitted while the store is running to save resources,
but because stores can be restarted, collection happens on the `reportingScope` which must outlive the store
itself to ensure proper cleanup and not lose data on lifecycle changes.
:::

1. `collectMetrics` installs a [decorator](/plugins/decorators.md) that measures the store pipeline and returns
   a `Metrics` implementation. This decorator returns you a `Metrics` instance that is attached to the store.
   Keep one `Metrics` instance per store: the decorator will use it to push events and update the data you get when you
   call `Metrics.snapshot()`. If you need to capture the latest data, you can use that interface to develop a custom
   reporting logic.
    - `offloadContext` moves computation/flushing off the main dispatcher. That's highly recommended to remove metric
      collection overhead from your main store's logic.
    - `windowSeconds` controls the sliding window for throughput.
    - `emaAlpha` sets smoothing for [EMA](https://en.wikipedia.org/wiki/Moving_average#Exponential_moving_average)-based
      averages.
2. `reportMetrics` installs a plugin that provides the default logic for metric flushing:
   it snapshots the collector on a fixed interval and delivers the data to the `MetricsSink` you give it.
    - Set `interval = Duration.INFINITE` to disable periodic snapshots and keep on-stop flushing only.
    - If your sink is slow, the plugin will sacrifice reporting frequency by dropping the oldest snapshots without
      sacrificing overall data integrity.

### 3. Implement a Sink that will send metrics

This part is on you. You need a place where you will send the metrics, such as a backend endpoint, log ingestion
infra, or a monitoring service.

To make your job easier, the library provides `Sink` decorators to format metrics for ingestion by:

- [OpenTelemetry](https://opentelemetry.io/),
- [Prometheus](https://prometheus.io/),
- [Open Metrics](https://openmetrics.io/),
- RESTful JSON endpoints.
- Plaintext/file-based loggers.

Built-in sinks:

- `LoggingJsonMetricsSink()` – serializes `MetricsSnapshot` to JSON and logs it via `StoreLogger` implementation.
- `OpenMetricsSink()` – emits [OpenMetrics](https://openmetrics.io/) text with `# EOF`, suitable for Prometheus HTTP
  endpoints.
- `PrometheusSink` – same output without the EOF line (Prometheus 0.0.4 exposition).
- `OtlpJsonMetricsSink` – produces OTLP Metrics JSON ready for OpenTelemetry collectors.
- `ConsoleSink`, `StoreLoggerSink`, `AppendableStringSink`, `MappingSink`, `JsonSink`, and `NoopSink` building
  blocks for quick wiring, tests, debug builds, or custom transport.

Pass `surfaceVersion` to downgrade emitted payloads for older consumers; otherwise the snapshot’s schema version is
used.

::::tip[Only release builds]
Reminder: you should only send metrics on release builds of your app to not pollute prod data.
::::

## Performance Overhead

You can find fresh benchmark results on CI and the source code in the `benchmarks` module.
In raw numbers, the results are (on a MacBook Pro M1 2021):

- Baseline: 0.342 ± 0.003 us/10k intents (~813 ns/intent)
- With Metrics: 1.029 ± 0.014 us/10k intents (~4382 ns/intent)

According to these, the overhead of metric collection is ~5.39x for a workflow with a single intent/state update path
compared to an identical configuration without metrics.

That looks like a big hit on paper, but in practice the hit is so small it's basically a rounding error.
With metrics enabled, you can still easily process 1000 intents in a single frame (16ms).

**Metrics becomes a meaningful CPU cost only if you process tens of thousands of
intents per second on a single hot path**

## Visualizing Metrics in the Debugger

FlowMVI's [Remote Debugger](/misc/debugging.md) can display metrics collected from your stores in real-time.
This allows you to monitor store performance directly in the IDE plugin or desktop app without setting up
external monitoring infrastructure.
