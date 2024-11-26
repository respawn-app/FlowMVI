package pro.respawn.flowmvi.decorators

import kotlinx.coroutines.withTimeoutOrNull
import pro.respawn.flowmvi.annotation.ExperimentalFlowMVIAPI
import pro.respawn.flowmvi.api.FlowMVIDSL
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.PipelineContext
import pro.respawn.flowmvi.decorator.PluginDecorator
import pro.respawn.flowmvi.decorator.decorator
import pro.respawn.flowmvi.dsl.StoreBuilder
import kotlin.time.Duration

@ExperimentalFlowMVIAPI
@FlowMVIDSL
public inline fun <S : MVIState, I : MVIIntent, A : MVIAction> intentTimeoutDecorator(
    timeout: Duration,
    crossinline onTimeout: suspend PipelineContext<S, I, A>.(attempted: I) -> I? = { null },
): PluginDecorator<S, I, A> = decorator {
    onIntent { chain, intent ->
        withTimeoutOrNull(timeout) {
            // can also return null so exit early to not confuse the 2 outcomes
            return@withTimeoutOrNull with(chain) { onIntent(intent) }
        } ?: onTimeout(intent)
    }
}

@ExperimentalFlowMVIAPI
@FlowMVIDSL
public inline fun <S : MVIState, I : MVIIntent, A : MVIAction> StoreBuilder<S, I, A>.timeoutIntents(
    timeout: Duration,
    crossinline onTimeout: suspend PipelineContext<S, I, A>.(attempted: I) -> I? = { null },
): Unit = install(intentTimeoutDecorator(timeout, onTimeout))
