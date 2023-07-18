package pro.respawn.flowmvi.android.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.flowWithLifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

// credits: https://proandroiddev.com/how-to-collect-flows-lifecycle-aware-in-jetpack-compose-babd53582d0b

/**
 * Create and remember a new [Flow] that is only collected when [lifecycleOwner] is in [lifecycleState]
 */
@Composable
public fun <T> rememberLifecycleFlow(
    flow: Flow<T>,
    lifecycleState: Lifecycle.State = Lifecycle.State.STARTED,
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
): Flow<T> = remember(flow, lifecycleOwner, lifecycleState) {
    flow.flowWithLifecycle(lifecycleOwner.lifecycle, lifecycleState)
}

/**
 * Create and collect a [Flow] that is only collected when [LocalLifecycleOwner] is in [lifecycleState]
 */
@Composable
@Deprecated("Use Androidx collectAsStateWithLifecycle instead")
public fun <T : R, R> Flow<T>.collectAsStateOnLifecycle(
    initial: R,
    context: CoroutineContext = EmptyCoroutineContext,
    lifecycleState: Lifecycle.State = Lifecycle.State.STARTED,
): State<R> = collectAsStateWithLifecycle(initialValue = initial, minActiveState = lifecycleState, context = context)

/**
 * Create and collect a [Flow] that is only collected when [LocalLifecycleOwner] is in [lifecycleState]
 */
@Suppress("StateFlowValueCalledInComposition")
@Composable
@Deprecated("Use Androidx collectAsStateWithLifecycle instead")
public fun <T> StateFlow<T>.collectAsStateOnLifecycle(
    context: CoroutineContext = EmptyCoroutineContext,
    lifecycleState: Lifecycle.State = Lifecycle.State.STARTED,
): State<T> = collectAsStateWithLifecycle(minActiveState = lifecycleState, context = context)

/**
 * Create and collect a [Flow] that is only collected when [LocalLifecycleOwner] is in [lifecycleState]
 */
@Composable
@Suppress("ComposableParametersOrdering", "ComposableNaming", "ComposableFunctionName")
@Deprecated("Use Androidx collectAsStateWithLifecycle instead")
public fun <T> Flow<T>.collectOnLifecycle(
    lifecycleState: Lifecycle.State = Lifecycle.State.STARTED,
    collector: suspend CoroutineScope.(T) -> Unit,
) {
    val lifecycleFlow = rememberLifecycleFlow(this, lifecycleState)
    LaunchedEffect(lifecycleFlow) {
        // see [LifecycleOwner.subscribe] in :android for reasoning
        withContext(Dispatchers.Main.immediate) {
            lifecycleFlow.collect {
                collector(it)
            }
        }
    }
}
