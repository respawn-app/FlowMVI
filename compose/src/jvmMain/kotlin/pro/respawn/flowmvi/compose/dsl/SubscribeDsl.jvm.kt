@file:Suppress("NOTHING_TO_INLINE")

package pro.respawn.flowmvi.compose.dsl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import kotlinx.coroutines.CoroutineScope
import pro.respawn.flowmvi.api.DelicateStoreApi
import pro.respawn.flowmvi.api.FlowMVIDSL
import pro.respawn.flowmvi.api.ImmutableStore
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.dsl.subscribe

/**
 * A function to subscribe to the store that follows the system lifecycle.
 *
 * * This function will assign the store a new subscriber when invoked, then populate the returned [State] with new states.
 * * Provided [consume] parameter will be used to consume actions that come from the store.
 * * Store's subscribers will **not** wait until the store is launched when they subscribe to the store.
 *   Such subscribers will not receive state updates or actions. Don't forget to launch the store.
 *
 * @param consume a lambda to consume actions with.
 * @return the [State] that contains the [ImmutableStore.state].
 * @see ImmutableStore.subscribe
 */
@OptIn(DelicateStoreApi::class)
@FlowMVIDSL
@Composable
public actual inline fun <S : MVIState, I : MVIIntent, A : MVIAction> ImmutableStore<S, I, A>.subscribe(
    noinline consume: suspend CoroutineScope.(action: A) -> Unit
): State<S> {
    val state = remember(this) { mutableStateOf(state) }
    val block by rememberUpdatedState(consume)
    LaunchedEffect(this@subscribe) {
        subscribe(
            store = this@subscribe,
            render = { state.value = it },
            consume = { block(it) }
        ).join()
    }
    return state
}

/**
 * A function to subscribe to the store that follows the system lifecycle.
 *
 * * This function will not collect [MVIAction]s.
 * * This function will assign the store a new subscriber when invoked, then populate the returned [State] with new states.
 * * Store's subscribers will **not** wait until the store is launched when they subscribe to the store.
 *   Such subscribers will not receive state updates or actions. Don't forget to launch the store.
 *   upon leaving that state, the function will unsubscribe.
 * @return the [State] that contains the [ImmutableStore.state].
 * @see ImmutableStore.subscribe
 */
@OptIn(DelicateStoreApi::class)
@FlowMVIDSL
@Composable
public actual inline fun <S : MVIState, I : MVIIntent, A : MVIAction> ImmutableStore<S, I, A>.subscribe(): State<S> {
    val state = remember(this) { mutableStateOf(state) }
    LaunchedEffect(this@subscribe) {
        subscribe(
            store = this@subscribe,
            render = { state.value = it }
        ).join()
    }
    return state
}
