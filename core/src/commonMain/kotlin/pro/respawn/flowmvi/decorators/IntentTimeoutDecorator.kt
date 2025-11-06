@file:MustUseReturnValue

package pro.respawn.flowmvi.decorators

import kotlinx.coroutines.withTimeoutOrNull
import pro.respawn.flowmvi.annotation.ExperimentalFlowMVIAPI
import pro.respawn.flowmvi.api.FlowMVIDSL
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.PipelineContext
import pro.respawn.flowmvi.api.StorePlugin
import pro.respawn.flowmvi.decorator.PluginDecorator
import pro.respawn.flowmvi.decorator.decorator
import pro.respawn.flowmvi.dsl.StoreBuilder
import pro.respawn.flowmvi.exceptions.StoreTimeoutException
import kotlin.time.Duration

/**
 * Creates a new decorator that runs each [StorePlugin.onIntent] through a time-out block.
 *
 * If the request did not complete within the [timeout], then [onTimeout] is called,
 * which by default throws a [StoreTimeoutException].
 *
 * @throws StoreTimeoutException
 */
@ExperimentalFlowMVIAPI
@FlowMVIDSL
public fun <S : MVIState, I : MVIIntent, A : MVIAction> intentTimeoutDecorator(
    timeout: Duration,
    name: String? = "IntentTimeout",
    onTimeout: suspend PipelineContext<S, I, A>.(I) -> I? = { throw StoreTimeoutException(timeout) },
): PluginDecorator<S, I, A> = decorator {
    this.name = name
    onIntent { chain, intent ->
        val outcome = withTimeoutOrNull(timeout) {
            with(chain) { onIntent(intent) } to true
        } ?: return@onIntent onTimeout(intent)
        outcome.first
    }
}

/**
 * Installs a new [intentTimeoutDecorator] for all intents in this store.
 */
@IgnorableReturnValue
@ExperimentalFlowMVIAPI
@FlowMVIDSL
public fun <S : MVIState, I : MVIIntent, A : MVIAction> StoreBuilder<S, I, A>.timeoutIntents(
    timeout: Duration,
    name: String? = "IntentTimeout",
    onTimeout: suspend PipelineContext<S, I, A>.(I) -> I? = { throw StoreTimeoutException(timeout) },
): Unit = install(intentTimeoutDecorator(timeout, name, onTimeout))
