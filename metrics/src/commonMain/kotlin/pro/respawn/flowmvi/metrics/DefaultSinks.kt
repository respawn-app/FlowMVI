package pro.respawn.flowmvi.metrics

import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.Json
import pro.respawn.flowmvi.logging.PlatformStoreLogger
import pro.respawn.flowmvi.logging.StoreLogLevel
import pro.respawn.flowmvi.logging.StoreLogger

/** Type alias for sinks that consume [MetricsSnapshot]. */
public typealias MetricsSink = Sink<MetricsSnapshot>

/** No-op sink used by default to disable metrics emission. */
public object NoopSink : Sink<Nothing> {

    override fun emit(value: Nothing): Unit = Unit
}

/** Sink that writes snapshots using [toString] to stdout. */
public class ConsoleSink<T> : Sink<T> {

    override fun emit(value: T): Unit = println(value)
}

/** Sink that logs strings through a [StoreLogger] (defaults to [PlatformStoreLogger]). */
public fun StoreLoggerSink(
    logger: StoreLogger = PlatformStoreLogger,
    level: StoreLogLevel = StoreLogLevel.Trace,
    tag: String = "Metrics",
): Sink<String> = Sink { value -> logger.log(level, tag) { value } }

/** Sink that appends strings to an [Appendable], one line per emit. */
public class AppendableStringSink(private val appendable: Appendable) : Sink<String> {

    override fun emit(value: String) {
        appendable.appendLine(value)
    }
}

/** Decorator that maps values before passing them to [delegate]. */
public inline fun <T, R> MappingSink(
    delegate: Sink<R>,
    crossinline map: (T) -> R,
): Sink<T> = Sink { value -> delegate.emit(map(value)) }

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
