@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package pro.respawn.flowmvi.android

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import pro.respawn.flowmvi.ActionShareBehavior
import pro.respawn.flowmvi.DelicateStoreApi
import pro.respawn.flowmvi.MVIAction
import pro.respawn.flowmvi.MVIIntent
import pro.respawn.flowmvi.MVIProvider
import pro.respawn.flowmvi.MVIState
import pro.respawn.flowmvi.MVIStore
import pro.respawn.flowmvi.Recover
import pro.respawn.flowmvi.ReducerScope
import pro.respawn.flowmvi.catchExceptions
import pro.respawn.flowmvi.launchedStore
import pro.respawn.flowmvi.updateState
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.coroutines.CoroutineContext

/**
 * A [ViewModel] that uses [MVIStore] internally to provide a convenient base class.
 * Only functions of the [MVIProvider] are made public, everything else happens through intents and actions.
 * If you want to add error handling for [reduce], override [recover] (default implementation throws immediately)
 * @param initialState the state to set when creating the view model.
 * @See MVIStore
 * @See pro.respawn.flowmvi.MVISubscriber
 * @See MVIProvider
 */
public abstract class MVIViewModel<S : MVIState, I : MVIIntent, A : MVIAction>(
    initialState: S,
) : ViewModel(), ReducerScope<S, I, A>, MVIProvider<S, I, A> {

    /**
     * [reduce] will be launched sequentially, on main thread, for each intent that comes from the view.
     * Intents will be processed in the order they come in.
     * Change the thread as needed
     * @See [MVIProvider.send]
     * @see [MVIStore]
     */
    protected abstract suspend fun reduce(intent: I)

    /**
     * Delegates to [MVIStore]'s recover block.
     */
    protected open fun recover(from: Exception): S = throw from

    /**
     * Overriding this field, don't forget to call [MVIStore.start] yourself.
     */
    protected open val store: MVIStore<S, I, A> by launchedStore(
        scope = viewModelScope,
        initial = initialState,
        behavior = ActionShareBehavior.Distribute(), // required for lifecycle awareness
        recover = { recover(it) },
        reduce = { this@MVIViewModel.reduce(it) },
    )

    @DelicateStoreApi
    override val state: S get() = store.state
    override val scope: CoroutineScope get() = viewModelScope
    override val actions: Flow<A> get() = store.actions
    override val states: StateFlow<S> get() = store.states
    override fun send(intent: I): Unit = store.send(intent)
    override fun send(action: A): Unit = store.send(action)
    override suspend fun updateState(transform: suspend S.() -> S): S = store.updateState(transform)
    final override suspend fun <R> withState(block: suspend S.() -> R): R = store.withState(block)
    override fun launchRecovering(
        context: CoroutineContext,
        start: CoroutineStart,
        recover: Recover<S>?,
        block: suspend CoroutineScope.() -> Unit,
    ): Job = store.launchRecovering(viewModelScope, context, start, recover, block)

    // TODO: With context receivers stable, this below can be removed

    /**
     * Shorthand for [Flow.launchIn] in viewModelScope
     */
    protected fun <T> Flow<T>.consume(): Job = launchIn(viewModelScope)

    /**
     * Uses [recover] to reduce exceptions occurring in the flow to states.
     * Shorthand for [kotlinx.coroutines.flow.catch]
     */
    protected fun <T> Flow<T>.recover(): Flow<T> = catchExceptions { updateState { recover(it) } }


    /**
     * Delegates to [MVIStore.updateState]
     */
    @JvmName("updateStateTyped")
    protected suspend inline fun <reified T : S> updateState(
        @BuilderInference crossinline transform: suspend T.() -> S
    ): S = store.updateState(transform)

    /**
     * Delegates to [MVIStore.updateState]
     */
    @JvmName("withStateTyped")
    protected suspend inline fun <reified T : S, R> withState(
        @BuilderInference crossinline block: suspend T.() -> R
    ) {
        contract {
            callsInPlace(block, InvocationKind.AT_MOST_ONCE)
        }
        return store.withState { (this as? T)?.let { it.block() } }
    }
}
