package pro.respawn.flowmvi.provider

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import pro.respawn.flowmvi.ActionShareBehavior
import pro.respawn.flowmvi.DelicateStoreApi
import pro.respawn.flowmvi.FlowMVIDSL
import pro.respawn.flowmvi.MVIAction
import pro.respawn.flowmvi.MVIIntent
import pro.respawn.flowmvi.MVIState
import pro.respawn.flowmvi.MVIStore
import pro.respawn.flowmvi.Recover
import pro.respawn.flowmvi.Reducer
import pro.respawn.flowmvi.catchExceptions
import pro.respawn.flowmvi.lazyStore
import pro.respawn.flowmvi.updateState
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.coroutines.CoroutineContext
import kotlin.jvm.JvmName
import pro.respawn.flowmvi.recover
import kotlin.coroutines.EmptyCoroutineContext

public abstract class StoreProvider<S : MVIState, I : MVIIntent, A : MVIAction>(
    initial: S,
    behavior: ActionShareBehavior = ActionShareBehavior.Distribute()
) : Reducer<S, I>, MVIStore<S, I, A> {

    protected open fun CoroutineScope.onStart(): Unit = Unit

    protected open val store: MVIStore<S, I, A> by lazyStore(
        initial = initial,
        behavior = behavior,
        recover = { recover(it) },
        reduce = { scope.reduce(it) },
    )

    @DelicateStoreApi
    override val state: S get() = store.state
    override val states: StateFlow<S> get() = store.states
    override val actions: Flow<A> get() = store.actions
    override fun send(intent: I): Unit = store.send(intent)
    override fun send(action: A): Unit = store.send(action)
    override fun start(scope: CoroutineScope): Job = store.start(scope).also { scope.onStart() }
    override suspend fun updateState(transform: suspend S.() -> S): S = store.updateState(transform)
    override suspend fun <R> withState(block: suspend S.() -> R): R = store.withState(block)

    @FlowMVIDSL
    final override fun launchRecovering(
        scope: CoroutineScope,
        context: CoroutineContext,
        start: CoroutineStart,
        recover: Recover<S>?,
        block: suspend CoroutineScope.() -> Unit
    ): Job = store.launchRecovering(scope, context, start, recover, block)

    @FlowMVIDSL
    @JvmName("launchRecoveringWithReceiver")
    protected fun CoroutineScope.launchRecovering(
        context: CoroutineContext = EmptyCoroutineContext,
        start: CoroutineStart = CoroutineStart.DEFAULT,
        recover: Recover<S>? = this@StoreProvider.recover,
        block: suspend CoroutineScope.() -> Unit
    ): Job = store.launchRecovering(this, context, start, recover, block)

    /**
     * Uses [recover] to reduce exceptions occurring in the flow to states.
     * Shorthand for [kotlinx.coroutines.flow.catch]
     */
    protected fun <T> Flow<T>.recover(): Flow<T> = catchExceptions { updateState { recover(it) } }

    @JvmName("updateStateTyped")
    protected suspend inline fun <reified T : S> updateState(
        @BuilderInference crossinline transform: suspend T.() -> S
    ): S = store.updateState(transform)

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
