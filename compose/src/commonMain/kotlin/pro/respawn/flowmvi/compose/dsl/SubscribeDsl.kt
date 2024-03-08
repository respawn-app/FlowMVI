package pro.respawn.flowmvi.compose.dsl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import pro.respawn.flowmvi.compose.api.SubscriberLifecycleOwner
import pro.respawn.flowmvi.compose.api.SubscriptionMode
import pro.respawn.flowmvi.dsl.subscribe
import pro.respawn.flowmvi.util.immediateOrDefault

@OptIn(DelicateStoreApi::class)
@Suppress("ComposableParametersOrdering")
@Composable
@FlowMVIDSL
public inline fun <S : MVIState, I : MVIIntent, A : MVIAction> ImmutableStore<S, I, A>.subscribe(
    mode: SubscriptionMode = SubscriptionMode.Started,
    owner: SubscriberLifecycleOwner = CurrentLifecycle,
    noinline consume: suspend CoroutineScope.(action: A) -> Unit,
): State<S> {
    val state = remember(this) { mutableStateOf(state) }
    val block by rememberUpdatedState(consume)
    LaunchedEffect(this@subscribe, mode, owner) {
        withContext(Dispatchers.Main.immediateOrDefault) {
            owner.repeatOnLifecycle(mode) {
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


@OptIn(DelicateStoreApi::class)
@Composable
@FlowMVIDSL
public inline fun <S : MVIState, I : MVIIntent, A : MVIAction> ImmutableStore<S, I, A>.subscribe(
    mode: SubscriptionMode = SubscriptionMode.Visible,
    owner: SubscriberLifecycleOwner = CurrentLifecycle,
): State<S> {
    val state = remember(this) { mutableStateOf(state) }
    LaunchedEffect(this@subscribe, mode, owner) {
        withContext(Dispatchers.Main.immediateOrDefault) {
            owner.repeatOnLifecycle(mode) {
                subscribe(
                    store = this@subscribe,
                    render = { state.value = it }
                ).join()
            }
        }
    }
    return state
}
