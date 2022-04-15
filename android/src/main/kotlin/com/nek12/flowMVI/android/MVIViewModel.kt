package com.nek12.flowMVI.android

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nek12.flowMVI.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * A [ViewModel] that uses [MVIStore] internally to provide a convenient base class for you to implement.
 * Override [initialState] and [reduce] and you're good to go.
 * Do not try to expose public functions in this view model, MVI doesn't work this way. The only functions you
 * will expose are those in [MVIProvider], and that's it, everything else happens inside the [MVIViewModel]
 * You can inject this view model into [MVIView.provider] field
 * If you want to have error handling for [reduce], implement [recover] (default implementation throws immediately)
 * @See MVIStore
 * @See MVIView
 */
abstract class MVIViewModel<S : MVIState, I : MVIIntent, A : MVIAction> : ViewModel(), MVIProvider<S, I, A> {

    private val store: MVIStore<S, I, A> by lazy {
        MVIStore(
            viewModelScope,
            initialState,
            ::recover,
            ::reduce
        )
    }

    protected abstract val initialState: S
    protected abstract suspend fun reduce(intent: I): S

    protected open fun recover(from: Exception): S = throw from

    protected val currentState: S get() = store.currentState

    override val actions get() = store.actions
    override val states: StateFlow<S> = store.states
    override fun send(intent: I) = store.send(intent)
    protected fun send(action: A) = store.send(action)
    protected fun set(state: S) = store.set(state)

    /**
     * Launch a coroutine that emits a new state. It is advisable to [recover] from any errors
     */
    protected fun launchForState(
        context: CoroutineContext = EmptyCoroutineContext,
        start: CoroutineStart = CoroutineStart.DEFAULT,
        recover: suspend CoroutineScope.(Exception) -> S = { this@MVIViewModel.recover(it) },
        call: suspend CoroutineScope.() -> S,
    ) = store.launchForState(viewModelScope, context, start, recover, call)

    /**
     * For use with operators such as [onEach]. A shorthand for [launchIn] (viewModelScope)
     */
    protected fun <T> Flow<T>.consume() = launchIn(viewModelScope)

    /**
     * Execute [block] if current state is [T] else just return [currentState].
     */
    protected inline fun <reified T : S> withState(block: T.() -> S): S {
        return (currentState as? T)?.let(block) ?: currentState
    }
}
