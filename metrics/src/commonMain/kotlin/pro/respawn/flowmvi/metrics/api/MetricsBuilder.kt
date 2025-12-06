package pro.respawn.flowmvi.metrics.api

import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.decorator.PluginDecorator
import pro.respawn.flowmvi.metrics.MetricsCollector

public class MetricsBuilder<S : MVIState, I : MVIIntent, A : MVIAction> internal constructor(
    internal val collector: MetricsCollector<S, I, A>
) : Metrics by collector {

    internal fun asDecorator(name: String?): PluginDecorator<S, I, A> = collector.asDecorator(name)

    public companion object
}
