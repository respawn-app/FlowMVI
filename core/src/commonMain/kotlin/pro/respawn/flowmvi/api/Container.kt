package pro.respawn.flowmvi.api

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job

/**
 * A simple class that delegates to the [store] property.
 */
public interface Container<out S : MVIState, in I : MVIIntent, out A : MVIAction> : Store<S, I, A> {

    public val store: Store<S, I, A>

    override fun close(): Unit = store.close()
    override fun send(intent: I): Unit = store.send(intent)
    override val initial: S get() = store.initial
    override fun start(scope: CoroutineScope): Job = store.start(scope)
    override suspend fun emit(intent: I): Unit = store.emit(intent)
    override fun CoroutineScope.subscribe(
        block: suspend Provider<S, I, A>.() -> Unit
    ): Job = with(store) { subscribe(block) }
}
