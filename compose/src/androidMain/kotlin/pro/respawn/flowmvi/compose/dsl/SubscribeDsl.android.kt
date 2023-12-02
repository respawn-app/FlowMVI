@file:Suppress("NOTHING_TO_INLINE")

package pro.respawn.flowmvi.compose.dsl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import pro.respawn.flowmvi.android.subscribe
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
 * @param lifecycleState the minimum lifecycle state that should be reached in order to subscribe to the store,
 *   upon leaving that state, the function will unsubscribe.
 * @param consume a lambda to consume actions with.
 * @return the [State] that contains the current state.
 * @see ImmutableStore.subscribe
 * @see subscribe
 */
@OptIn(DelicateStoreApi::class)
@Suppress("NOTHING_TO_INLINE", "ComposableParametersOrdering")
@Composable
@FlowMVIDSL
public inline fun <S : MVIState, I : MVIIntent, A : MVIAction> ImmutableStore<S, I, A>.subscribe(
    lifecycleState: Lifecycle.State = Lifecycle.State.STARTED,
    noinline consume: suspend CoroutineScope.(action: A) -> Unit,
): State<S> {
    val owner = LocalLifecycleOwner.current
    val state = remember(this) { mutableStateOf(state) }
    val block by rememberUpdatedState(consume)
    LaunchedEffect(this@subscribe, lifecycleState, owner) {
        withContext(Dispatchers.Main.immediate) {
            owner.repeatOnLifecycle(lifecycleState) {
                subscribe(
                    store = this@subscribe,
                    consume = { block(it) },
                    render = { state.value = it }
                ).join()
            }
        }
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
 * @param lifecycleState the minimum lifecycle state that should be reached in order to subscribe to the store,
 *   upon leaving that state, the function will unsubscribe.
 * @return the [State] that contains the current state.
 * @see ImmutableStore.subscribe
 * @see subscribe
 */
@OptIn(DelicateStoreApi::class)
@Suppress("NOTHING_TO_INLINE")
@Composable
@FlowMVIDSL
public inline fun <S : MVIState, I : MVIIntent, A : MVIAction> ImmutableStore<S, I, A>.subscribe(
    lifecycleState: Lifecycle.State = Lifecycle.State.STARTED,
): State<S> {
    val owner = LocalLifecycleOwner.current
    val state = remember(this) { mutableStateOf(state) }
    LaunchedEffect(this@subscribe, lifecycleState, owner) {
        withContext(Dispatchers.Main.immediate) {
            owner.repeatOnLifecycle(lifecycleState) {
                subscribe(
                    store = this@subscribe,
                    render = { state.value = it }
                ).join()
            }
        }
    }
    return state
}

/**
 * Alias for [pro.respawn.flowmvi.compose.dsl.subscribe] with [Lifecycle.State.STARTED].
 **/
@FlowMVIDSL
@Composable
public actual inline fun <S : MVIState, I : MVIIntent, A : MVIAction> ImmutableStore<S, I, A>.subscribe(): State<S> =
    subscribe(Lifecycle.State.STARTED)

/**
 * Alias for [pro.respawn.flowmvi.compose.dsl.subscribe] with [Lifecycle.State.STARTED].
 **/
@FlowMVIDSL
@Composable
@Suppress("NOTHING_TO_INLINE", "ComposableParametersOrdering")
public actual inline fun <S : MVIState, I : MVIIntent, A : MVIAction> ImmutableStore<S, I, A>.subscribe(
    noinline consume: suspend CoroutineScope.(action: A) -> Unit
): State<S> = subscribe(Lifecycle.State.STARTED, consume)