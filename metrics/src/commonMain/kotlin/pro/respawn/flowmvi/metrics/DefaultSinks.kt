package pro.respawn.flowmvi.metrics

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.Json
import pro.respawn.flowmvi.logging.PlatformStoreLogger
import pro.respawn.flowmvi.logging.StoreLogLevel
import pro.respawn.flowmvi.logging.StoreLogger
import pro.respawn.flowmvi.metrics.api.MetricsSnapshot
import pro.respawn.flowmvi.metrics.api.Sink

/** Type alias for sinks that consume [MetricsSnapshot]. */
public typealias MetricsSink = Sink<MetricsSnapshot>

/** No-op sink used by default to disable metrics emission. */
public fun <T> NoopSink(): Sink<T> = Sink {}

/** Sink that writes snapshots using [toString] to stdout. */
public class ConsoleSink<T> : Sink<T> {

    override suspend fun emit(value: T): Unit = println(value)
}

/** Sink that logs strings through a [StoreLogger] (defaults to [PlatformStoreLogger]). */
public fun StoreLoggerSink(
    logger: StoreLogger = PlatformStoreLogger,
    level: StoreLogLevel = StoreLogLevel.Trace,
    tag: String = "Metrics",
): Sink<String> = Sink { value -> logger.log(level, tag) { value } }

/** Sink that appends strings to an [Appendable], one line per emit. */
public class AppendableStringSink(private val appendable: Appendable) : Sink<String> {

    override suspend fun emit(value: String) {
        appendable.appendLine(value)
    }
}

/** Decorator that maps values before passing them to [delegate]. */
public inline fun <T, R> MappingSink(
    delegate: Sink<R>,
    crossinline map: (T) -> R,
): Sink<T> = Sink { value -> delegate.emit(map(value)) }

/**
 * Fan-out sink that forwards emissions to all [delegates] in parallel.
 *
 * Set [parallel] to `false` to execute delegates sequentially.
 */
public fun <T> CompositeSink(
    vararg delegates: Sink<T>,
    parallel: Boolean = true,
): Sink<T> = when {
    delegates.isEmpty() -> NoopSink()
    delegates.size == 1 -> delegates[0]
    parallel -> Sink { value -> coroutineScope { delegates.map { async { it.emit(value) } }.awaitAll() } }
    else -> Sink { value -> delegates.forEach { it.emit(value) } }
}

/** Serializes values with the provided [serializer] and forwards JSON strings to [delegate]. */
public fun <T> JsonSink(
    delegate: Sink<String>,
    json: Json,
    serializer: SerializationStrategy<T>
): Sink<T> = MappingSink(delegate) { json.encodeToString(serializer, it) }

/** Ready-to-use JSON sink that logs via [StoreLogger] (PlatformStoreLogger by default). */
public fun LoggingJsonMetricsSink(
    json: Json,
    logger: StoreLogger = PlatformStoreLogger,
    level: StoreLogLevel = StoreLogLevel.Trace,
    tag: String = "Metrics",
): MetricsSink = JsonSink(StoreLoggerSink(logger, level, tag), json, MetricsSnapshot.serializer())
