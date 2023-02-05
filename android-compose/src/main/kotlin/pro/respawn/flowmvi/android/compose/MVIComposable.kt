package pro.respawn.flowmvi.android.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.Lifecycle
import kotlinx.coroutines.Dispatchers
import pro.respawn.flowmvi.MVIAction
import pro.respawn.flowmvi.MVIIntent
import pro.respawn.flowmvi.MVIProvider
import pro.respawn.flowmvi.MVIState

/**
 * A function that introduces ConsumerScope to the content and ensures safe lifecycle-aware and efficient collection
 * of states and actions.
 * Usage:
 * ```
 * @Composable
 * fun HomeScreen() = MVIComposable(getViewModel<HomeViewModel>()) { state ->
 *     consume { action ->
 *         when(action) {
 *             /*...*/
 *         }
 *     }
 *     when(state) {
 *         //use state to render content
 *     }
 * }
 * ```
 * @param provider an MVIProvider (usually a viewModel) that handles this screen's logic
 * @param lifecycleState the minimum lifecycle state, in which the activity must be to receive actions/states
 * @param content the actual screen content. Will be recomposed each time a new state is received.
 */
@Composable
public fun <S : MVIState, I : MVIIntent, A : MVIAction, VM : MVIProvider<S, I, A>> MVIComposable(
    provider: VM,
    lifecycleState: Lifecycle.State = Lifecycle.State.STARTED,
    content: @Composable ConsumerScope<I, A>.(state: S) -> Unit,
) {
    val scope = rememberConsumerScope(provider, lifecycleState)

    // see [LifecycleOwner.subscribe] in :android for reasoning behind the dispatcher
    val state by provider.states.collectAsStateOnLifecycle(Dispatchers.Main.immediate, lifecycleState)

    content(scope, state)
}
