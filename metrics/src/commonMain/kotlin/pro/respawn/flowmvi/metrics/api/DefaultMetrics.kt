package pro.respawn.flowmvi.metrics.api

import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.decorator.PluginDecorator
import pro.respawn.flowmvi.metrics.MetricsCollector

/**
 * The default [Metrics] implementation provided by FlowMVI.
 *
 * As is, this object does nothing useful, and the returned [snapshot] will not contain any non-empty values.
 *
 * To actually update the [MetricsSnapshot] returned, install the [pro.respawn.flowmvi.metrics.dsl.metricsDecorator]
 * that will do the work of collecting metrics. (See [Metrics] for more details)
 *
 * An instance can be obtained from the [pro.respawn.flowmvi.metrics.dsl.metrics] DSL function.
 */
public class DefaultMetrics<S : MVIState, I : MVIIntent, A : MVIAction> internal constructor(
    internal val collector: MetricsCollector<S, I, A>
) : Metrics by collector {

    internal fun asDecorator(name: String?): PluginDecorator<S, I, A> = collector.asDecorator(name)

    /** Static helpers namespace. */
    public companion object
}
