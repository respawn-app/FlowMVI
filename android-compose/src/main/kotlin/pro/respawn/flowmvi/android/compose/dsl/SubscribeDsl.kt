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
import pro.respawn.flowmvi.api.FlowMVIDSL
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.Store
import pro.respawn.flowmvi.dsl.subscribe

@Composable
@FlowMVIDSL
@Suppress("NOTHING_TO_INLINE")
public inline fun <S : MVIState, I : MVIIntent, A : MVIAction> Store<S, I, A>.subscribe(
    lifecycleState: Lifecycle.State = Lifecycle.State.STARTED,
    noinline consume: (suspend CoroutineScope.(action: A) -> Unit)? = null
): State<S> {
    val owner = LocalLifecycleOwner.current
    val state = remember(this) { mutableStateOf(initial) }
    val block by rememberUpdatedState(consume)
    LaunchedEffect(owner, this, state) {
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
