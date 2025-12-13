package pro.respawn.flowmvi.metrics.api

/**
 * A sink, where values such as [Metrics] can be dumped by code such as the one used by the
 * [pro.respawn.flowmvi.metrics.dsl.metricsReporter] plugin
 *
 * Sinks can be composed and decorated to allow [Metrics] to be emitted to consumers.
 *
 * See predefined Sinks such as
 * - [pro.respawn.flowmvi.metrics.CompositeSink],
 * - [pro.respawn.flowmvi.metrics.MappingSink],
 * - [pro.respawn.flowmvi.metrics.JsonSink]
 * - [pro.respawn.flowmvi.metrics.ConsoleSink] or others.
 */
public fun interface Sink<T> {

    /**
     * Emits a value downstream.
     * If this method throws, the exception should be propagated to the consumer.
     * */
    public suspend fun emit(value: T)
}
