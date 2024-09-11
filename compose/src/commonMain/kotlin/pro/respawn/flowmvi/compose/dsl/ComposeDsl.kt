@file:OptIn(DelicateStoreApi::class)

package pro.respawn.flowmvi.compose.dsl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.rememberUpdatedState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import pro.respawn.flowmvi.api.DelicateStoreApi
import pro.respawn.flowmvi.api.FlowMVIDSL
import pro.respawn.flowmvi.api.ImmutableStore
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.SubscriberLifecycle
import pro.respawn.flowmvi.api.SubscriptionMode
import pro.respawn.flowmvi.dsl.subscribe
import pro.respawn.flowmvi.util.immediateOrDefault
import kotlin.jvm.JvmName

/**
 * A function to subscribe to the store that follows the system lifecycle.
 *
 * * This function will assign the store a new subscriber when invoked, then populate the returned [State] with new states.
 * * Provided [consume] parameter will be used to consume actions that come from the store.
 * * Store's subscribers will **not** wait until the store is launched when they subscribe to the store.
 *   Such subscribers will not receive state updates or actions. Don't forget to launch the store.
 *
 * @param mode the subscription mode that should be reached in order to subscribe to the store. At specified moments
 * in the UI lifecycle (Activity, Composable, Window etc), the store will subscribe and unsubscribe from the store.
 * @param consume a lambda to consume actions with.
 * @return the [State] that contains the current state.
 * @see ImmutableStore.subscribe
 * @see subscribe
 */
@Suppress("ComposableParametersOrdering")
@Composable
@FlowMVIDSL
@JvmName("subscribeConsume")
public fun <S : MVIState, I : MVIIntent, A : MVIAction> ImmutableStore<S, I, A>.subscribe(
    lifecycle: SubscriberLifecycle = DefaultLifecycle,
    mode: SubscriptionMode = SubscriptionMode.Started,
    consume: suspend CoroutineScope.(action: A) -> Unit,
): State<S> {
    val block by rememberUpdatedState(consume)
    return produceState(state, this, mode, lifecycle) {
        withContext(Dispatchers.Main.immediateOrDefault) {
            lifecycle.repeatOnLifecycle(mode) {
                subscribe(
                    store = this@subscribe,
                    consume = { block(it) },
                    render = { value = it }
                ).join()
            }
        }
    }
}

/**
 * A function to subscribe to the store that follows the system lifecycle.
 *
 * * This function will assign the store a new subscriber when invoked, then populate the returned [State] with new states.
 * * Store's subscribers will **not** wait until the store is launched when they subscribe to the store.
 *   Such subscribers will not receive state updates or actions. Don't forget to launch the store.
 *
 * @param mode the subscription mode that should be reached in order to subscribe to the store. At specified moments
 * in the UI lifecycle (Activity, Composable, Window etc), the store will subscribe and unsubscribe from the store.
 * @return the [State] that contains the current state.
 * @see ImmutableStore.subscribe
 * @see subscribe
 */
@Composable
@FlowMVIDSL
public fun <S : MVIState, I : MVIIntent, A : MVIAction> ImmutableStore<S, I, A>.subscribe(
    lifecycle: SubscriberLifecycle = DefaultLifecycle,
    mode: SubscriptionMode = SubscriptionMode.Started,
): State<S> = produceState(state, mode, lifecycle, this@subscribe) {
    withContext(Dispatchers.Main.immediateOrDefault) {
        lifecycle.repeatOnLifecycle(mode) {
            subscribe(
                store = this@subscribe,
                render = { value = it },
            ).join()
        }
    }
}
