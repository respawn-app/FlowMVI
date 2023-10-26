package pro.respawn.flowmvi.android.compose.dsl

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
import pro.respawn.flowmvi.api.DelicateStoreApi
import pro.respawn.flowmvi.api.FlowMVIDSL
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.Store
import pro.respawn.flowmvi.dsl.subscribe

/**
 * A function to subscribe to the store that follows the system lifecycle.
 * This function will assign the store a new subscriber when invoked,
 * then populate the returned [State] with new states.
 * Provided [consume] parameter will be used to consume actions that come from the store.
 * [consume] can be null (by default) if you have your actions disabled or don't want to receive them.
 *
 * @param lifecycleState the minimum lifecycle state that should be reached in order to subscribe to the store,
 *   upon leaving that state, the function will unsubscribe.
 * @param consume a lambda to consume actions with.
 *
 * @see Store.subscribe
 */
@OptIn(DelicateStoreApi::class)
@Suppress("NOTHING_TO_INLINE")
@Composable
@FlowMVIDSL
public inline fun <S : MVIState, I : MVIIntent, A : MVIAction> Store<S, I, A>.subscribe(
    lifecycleState: Lifecycle.State = Lifecycle.State.STARTED,
    noinline consume: (suspend CoroutineScope.(action: A) -> Unit)? = null
): State<S> {
    val owner = LocalLifecycleOwner.current
    val state = remember(this) { mutableStateOf(state) }
    val block by rememberUpdatedState(consume)
    LaunchedEffect(this, lifecycleState) {
        owner.repeatOnLifecycle(lifecycleState) {
            subscribe(
                store = this@subscribe,
                consume = block?.let { block -> { block(it) } },
                render = { state.value = it }
            )
        }
    }
    return state
}
