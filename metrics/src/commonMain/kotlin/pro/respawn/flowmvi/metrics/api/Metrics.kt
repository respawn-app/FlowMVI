package pro.respawn.flowmvi.metrics.api

/**
 * Objects that allows retrieving metrics [snapshot].
 *
 * To actually populate and collect metrics, use this Metrics object, such as the default one produced by
 * [pro.respawn.flowmvi.metrics.dsl.metrics] with the [pro.respawn.flowmvi.metrics.dsl.metricsDecorator] that will
 * populate the metrics returned by the [snapshot] with actual values.
 *
 * Consider using [pro.respawn.flowmvi.metrics.dsl.metricsReporter] plugin (or your custom plugin or logic) that will
 * periodically call [snapshot] and emit metrics to a [Sink] or custom destination.
 *
 * - The Metrics object should be scoped per-store-object (not per lifecycle).
 * - [snapshot] is safe to call when the store is stopped, but the metrics will always reflect the snapshot at the
 *   moment the store was stopped if the default [pro.respawn.flowmvi.metrics.dsl.metricsDecorator] was used.
 * - This method can be computationally intensive to compute values, and should ideally be called off main thread.
 */
public fun interface Metrics {

    /**
     * Capture a point-in-time [MetricsSnapshot] of the attached store.
     *
     * Please see the [Metrics] (class) docs for details and notes on this method.
     */
    public suspend fun snapshot(): MetricsSnapshot

    @Suppress("UndocumentedPublicProperty")
    public companion object
}
