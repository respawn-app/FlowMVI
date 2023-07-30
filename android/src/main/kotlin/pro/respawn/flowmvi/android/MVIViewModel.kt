@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package pro.respawn.flowmvi.android

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import pro.respawn.flowmvi.MVIStore
import pro.respawn.flowmvi.MutableStore
import pro.respawn.flowmvi.api.ActionReceiver
import pro.respawn.flowmvi.api.DelicateStoreApi
import pro.respawn.flowmvi.api.IntentReceiver
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.Provider
import pro.respawn.flowmvi.api.StateReceiver
import pro.respawn.flowmvi.api.Store
import pro.respawn.flowmvi.dsl.lazyStore
import pro.respawn.flowmvi.dsl.updateState
import pro.respawn.flowmvi.lazyStore
import pro.respawn.flowmvi.plugins.recover
import pro.respawn.flowmvi.plugins.reduce
import pro.respawn.flowmvi.updateState
import pro.respawn.flowmvi.util.catchExceptions
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.cancellation.CancellationException

/**
 * A [ViewModel] that uses [MVIStore] internally to provide a convenient base class.
 * Only functions of the [MVIProvider] are made public, everything else happens through intents and actions.
 * If you want to add error handling for [reduce], override [recover] (default implementation throws immediately)
 * @param initialState the state to set when creating the view model.
 * @See MVIStore
 * @See pro.respawn.flowmvi.MVISubscriber
 * @See MVIProvider
 */
@Deprecated(
    """
MVIViewModel is now deprecated. A better API was designed for MVIViewModels that is multiplatform,
extensible, and uses composition instead of locking you onto a specific base class.
To migrate, create a wrapper class that has a `store` property, inject dependencies into it normally, and build the store 
using store() function.
Please see example implementation in the project's repository or or consult with the documentation for a full guide.
"""
)
public abstract class MVIViewModel<S : MVIState, I : MVIIntent, A : MVIAction>(
    final override val initial: S,
) : ViewModel(), IntentReceiver<I>, StateReceiver<S>, ActionReceiver<A>, Store<S, I, A> {

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

    protected open suspend fun recover(e: Exception): S = throw e

    @DelicateStoreApi
    override fun useState(block: S.() -> S): Unit = store.useState(block)

    /**
     * Overriding this field, don't forget to call [MVIStore.start] yourself.
     */
    @Suppress("UNCHECKED_CAST", "DEPRECATION")
    protected open val store: MutableStore<S, I, A> by lazyStore<S, I, A>(viewModelScope) {
        recover {
            updateState { recover(it) }
            null
        }
        reduce { reduce(it) }
        initial(initial)
    } as Lazy<MutableStore<S, I, A>>

    override fun send(intent: I): Unit = store.send(intent)

    /**
     * @see MVIStore.send
     */
    public override suspend fun send(action: A): Unit = store.send(action)

    /**
     * @see MVIStore.updateState
     */
    override suspend fun updateState(transform: suspend S.() -> S): Unit = store.updateState(transform)

    /**
     * @see MVIStore.withState
     */
    override suspend fun <R> withState(block: suspend S.() -> R): R = store.withState(block)

    /**
     * @see MVIStore.launchRecovering
     */
    protected fun launchRecovering(
        context: CoroutineContext = EmptyCoroutineContext,
        start: CoroutineStart = CoroutineStart.DEFAULT,
        recover: suspend (Exception) -> S? = { this.recover(it) },
        block: suspend CoroutineScope.() -> Unit,
    ): Job = viewModelScope.launch(context, start) {
        try {
            supervisorScope(block)
        } catch (expected: CancellationException) {
            throw expected
        } catch (expected: Exception) {
            recover(expected)
        }
    }

    /**
     * Shorthand for [Flow.launchIn] in viewModelScope
     */
    protected fun <T> Flow<T>.consume(): Job = launchIn(viewModelScope)

    /**
     * Uses [recover] to reducer exceptions occurring in the flow to states.
     * Shorthand for [kotlinx.coroutines.flow.catch]
     */
    protected fun <T> Flow<T>.recover(): Flow<T> = catchExceptions { updateState { recover(it) } }

    /**
     * Delegates to [MVIStore.updateState]
     */
    @JvmName("updateStateTyped")
    protected suspend inline fun <reified T : S> updateState(
        @BuilderInference crossinline transform: suspend T.() -> S
    ) {
        contract {
            callsInPlace(transform, InvocationKind.AT_MOST_ONCE)
        }
        return store.updateState<T, S>(transform)
    }

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

    override fun start(scope: CoroutineScope): Job = error("Store is already started by the ViewModel")

    public override fun CoroutineScope.subscribe(
        block: suspend Provider<S, I, A>.() -> Unit
    ): Job = with(store) { subscribe(block) }
}
