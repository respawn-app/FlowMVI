@file:pro.respawn.flowmvi.annotation.MustUseReturnValues

package pro.respawn.flowmvi.dsl

import pro.respawn.flowmvi.api.FlowMVIDSL
import pro.respawn.flowmvi.api.LazyPlugin
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.StorePlugin

/**
 * Build a new [StorePlugin] using [StorePluginBuilder] lazily.
 * Plugin will be created upon first usage (i.e. installation).
 * Example:
 * ```kotlin
 * val plugin = lazyPlugin<State, Intent, Action> {
 *
 *     config.logger.info { "I am using config" }
 *
 *     onIntent { intent -> // this: PipelineContext
 *
 *     }
 * }
 * ```
 *
 * @see [StorePlugin]
 */
@FlowMVIDSL
public inline fun <S : MVIState, I : MVIIntent, A : MVIAction> lazyPlugin(
    @BuilderInference crossinline builder: LazyPluginBuilder<S, I, A>.() -> Unit,
): LazyPlugin<S, I, A> = LazyPlugin {
    LazyPluginBuilder<S, I, A>(it).apply(builder).build()
}

/**
 * Build a new [StorePlugin] using [StorePluginBuilder].
 * See [StoreBuilder.install] to install the plugin automatically.
 *
 * Example:
 * ```kotlin
 * val plugin = plugin<State, Intent, Action> {
 *     onIntent { intent -> // this: PipelineContext
 *         // Handle
 *     }
 * }
 * ```
 *
 * @see [StorePlugin]
 */
@FlowMVIDSL
public inline fun <S : MVIState, I : MVIIntent, A : MVIAction> plugin(
    @BuilderInference builder: StorePluginBuilder<S, I, A>.() -> Unit,
): StorePlugin<S, I, A> = StorePluginBuilder<S, I, A>().apply(builder).build()
