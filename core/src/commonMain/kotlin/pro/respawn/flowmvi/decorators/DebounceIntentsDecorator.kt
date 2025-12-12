@file:MustUseReturnValues

package pro.respawn.flowmvi.decorators

import kotlinx.atomicfu.atomic
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
import kotlin.time.Duration.Companion.ZERO

private class JobHandle {

    var job by atomic<Job?>(null)
        private set

    inline fun set(job: Job?) {
        this.job = job
    }
}

/**
 * Debounces incoming intents, mirroring the semantics of [kotlinx.coroutines.flow.debounce].
 *
 * Only the latest intent emitted after a period of inactivity (defined by [timeout]) is forwarded downstream.
 * Intents arriving within the timeout window cancel the pending delivery and schedule a new one.
 * A non-positive [timeout] forwards the intent immediately, matching flow's behavior.
 */
@ExperimentalFlowMVIAPI
@FlowMVIDSL
public fun <S : MVIState, I : MVIIntent, A : MVIAction> debounceIntentsDecorator(
    timeout: Duration,
    name: String? = "DebounceIntents",
): PluginDecorator<S, I, A> = debounceIntentsDecorator(name) { timeout }

/**
 * Debounces incoming intents using a dynamic timeout selector, mirroring [kotlinx.coroutines.flow.debounce].
 *
 * The selector is invoked per intent; each positive duration delays forwarding until no newer intents arrive.
 * Non-positive durations deliver immediately. Pending deliveries are cancelled when a new intent arrives.
 */
@ExperimentalFlowMVIAPI
@FlowMVIDSL
public fun <S : MVIState, I : MVIIntent, A : MVIAction> debounceIntentsDecorator(
    name: String? = "DebounceIntents",
    timeoutSelector: suspend PipelineContext<S, I, A>.(I) -> Duration,
): PluginDecorator<S, I, A> = decorator {
    this.name = name
    val handle = JobHandle()

    onIntent { chain, intent ->
        handle.job?.cancelAndJoin()
        val duration = timeoutSelector(intent)
        if (duration <= ZERO) return@onIntent with(chain) { onIntent(intent) }
        val job = launch {
            delay(duration)
            with(chain) { onIntent(intent) }
        }
        handle.set(job)
        null
    }
    onStop { child, e ->
        handle.set(null)
        child.run { onStop(e) }
    }
}

/**
 * Installs a [debounceIntentsDecorator] with a fixed timeout for all intents in this store.
 */
@ExperimentalFlowMVIAPI
@FlowMVIDSL
public fun <S : MVIState, I : MVIIntent, A : MVIAction> StoreBuilder<S, I, A>.debounceIntents(
    timeout: Duration,
    name: String? = "DebounceIntents",
): Unit = install(debounceIntentsDecorator(timeout, name))

/**
 * Installs a [debounceIntentsDecorator] with a dynamic timeout selector for all intents in this store.
 */
@ExperimentalFlowMVIAPI
@FlowMVIDSL
public fun <S : MVIState, I : MVIIntent, A : MVIAction> StoreBuilder<S, I, A>.debounceIntents(
    name: String? = "DebounceIntents",
    timeoutSelector: suspend PipelineContext<S, I, A>.(I) -> Duration,
): Unit = install(debounceIntentsDecorator(name, timeoutSelector))
