package pro.respawn.flowmvi.dsl

import pro.respawn.flowmvi.api.FlowMVIDSL
import pro.respawn.flowmvi.api.LazyPlugin
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.StoreConfiguration
import pro.respawn.flowmvi.api.StorePlugin

/**
 * A class that creates a [LazyPlugin].
 *
 * This is the same as [StorePluginBuilder], but has access to an additional [config] property that will be provided
 * when the store is created.
 */
public class LazyPluginBuilder<S : MVIState, I : MVIIntent, A : MVIAction> @PublishedApi internal constructor(
    public val config: StoreConfiguration<S>
) : StorePluginBuilder<S, I, A>()

/**
 * Build a new [StorePlugin] using [StorePluginBuilder] lazily.
 * Plugin will be created upon first usage (i.e. installation).
 * @see [StorePlugin]
 */
@FlowMVIDSL
public inline fun <S : MVIState, I : MVIIntent, A : MVIAction> lazyPlugin(
    @BuilderInference crossinline builder: LazyPluginBuilder<S, I, A>.() -> Unit,
): LazyPlugin<S, I, A> = LazyPlugin {
    LazyPluginBuilder<S, I, A>(it).apply(builder).build()
}
