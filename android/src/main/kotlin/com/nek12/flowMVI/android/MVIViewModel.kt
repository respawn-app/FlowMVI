@file:Suppress("MemberVisibilityCanBePrivate")

package com.nek12.flowMVI.android

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nek12.flowMVI.MVIAction
import com.nek12.flowMVI.MVIIntent
import com.nek12.flowMVI.MVIProvider
import com.nek12.flowMVI.MVIState
import com.nek12.flowMVI.MVIStore
import com.nek12.flowMVI.MVIView
import com.nek12.flowMVI.currentState
import com.nek12.flowMVI.launchForState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onEmpty
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * A [ViewModel] that uses [MVIStore] internally to provide a convenient base class.
 * Do not try to expose public functions in this view model, MVI doesn't work this way. The only functions you
 * should expose are those in [MVIProvider], everything else happens through intents and actions.
 * You can inject this view model into the [MVIView.provider] field
 * If you want to add error handling for [reduce], override [recover] (default implementation throws immediately)
 * @param initialState the state to set when creating the view model.
 * @See MVIStore
 * @See MVIView
 * @See MVIProvider
 */
abstract class MVIViewModel<S: MVIState, I: MVIIntent, A: MVIAction>(
    initialState: S,
): ViewModel(), MVIProvider<S, I, A> {

    private var isLaunched: Boolean = false

    /**
     * [reduce] will be launched in parallel, on main thread, for each intent that comes from the view.
     * To change thread, use [kotlinx.coroutines.withContext].
     */
    protected abstract suspend fun reduce(intent: I): S

    protected open fun recover(from: Exception): S = throw from

    /**
     * Overriding this field, don't forget to call [MVIStore.launch] yourself.
     */
    protected open val store: MVIStore<S, I, A> = MVIStore<S, I, A>(
        initialState = initialState,
        recover = ::recover,
        reduce = ::reduce
    ).apply { launch(viewModelScope) }

    override val actions get() = store.actions
    override val states: StateFlow<S> get() = store.states
    override fun send(intent: I) = store.send(intent)

    protected open fun send(action: A) = store.send(action)
    protected open fun set(state: S) = store.set(state)

    /**
     * Shorthand for [Flow.launchIn] in viewModelScope
     */
    protected fun <T> Flow<T>.consume() = launchIn(viewModelScope)

    /**
     * Uses [recover] to reduce exceptions occurring in the flow to states.
     * Shorthand for [kotlinx.coroutines.flow.catch]
     */
    protected fun <T> Flow<T>.recover() = catchExceptions { set(recover(it)) }

    /**
     * Sets this state when flow completes without emitting any values.
     */
    protected fun <T> Flow<T>.onEmpty(state: S) = onEmpty { set(state) }

    /**
     * For a flow of states (usually mapped using [kotlinx.coroutines.flow.map]), sends each state to the subscriber.
     */
    protected fun Flow<S>.setEach() = onEach { set(it) }

    /**
     * Launches a coroutine that emits a new state. It is advisable to [recover] from any errors
     */
    protected fun launchForState(
        context: CoroutineContext = EmptyCoroutineContext,
        start: CoroutineStart = CoroutineStart.DEFAULT,
        recover: suspend CoroutineScope.(Exception) -> S = { this@MVIViewModel.recover(it) },
        call: suspend CoroutineScope.() -> S,
    ) = store.launchForState(viewModelScope, context, start, recover, call)

    /**
     * Execute [block] if current state is [T] else just return [currentState].
     */
    protected inline fun <reified T: S> withState(block: T.() -> S): S {
        return (currentState as? T)?.let(block) ?: currentState
    }
}
