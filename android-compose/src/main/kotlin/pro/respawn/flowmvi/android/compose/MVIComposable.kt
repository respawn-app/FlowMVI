package pro.respawn.flowmvi.android.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.Lifecycle
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.Store

/**
 * A function that introduces ConsumerScope to the content and ensures safe lifecycle-aware and efficient collection
 * of states and actions.
 * Usage:
 * ```kotlin
 * @Composable
 * fun HomeScreen() = MVIComposable(getViewModel<HomeViewModel>()) { // this: ConsumerScope<S, I, A>
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
public fun <S : MVIState, I : MVIIntent, A : MVIAction> MVIComposable(
    store: Store<S, I, A>,
    lifecycleState: Lifecycle.State = Lifecycle.State.STARTED,
    @BuilderInference content: @Composable ConsumerScope<I, A>.(state: S) -> Unit,
) {
    val scope = rememberConsumerScope(store, lifecycleState)
    val state by scope.state
    content(scope, state)
}
