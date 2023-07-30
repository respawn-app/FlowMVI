package pro.respawn.flowmvi.interop

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.Store
import pro.respawn.flowmvi.dsl.subscribe

@OptIn(ExperimentalStdlibApi::class)
public class NativeStore<S : MVIState, I : MVIIntent, A : MVIAction>(
    private val store: Store<S, I, A>,
    autoStart: Boolean = false,
) : AutoCloseable {

    private val scope = CoroutineScope(Dispatchers.Main)
    public val initial: S = store.initial
    public val name: String = store.name

    init {
        if (autoStart) store.start(scope)
    }

    public fun subscribe(
        onAction: (action: A) -> Unit,
        onState: (state: S) -> Unit,
    ): AutoCloseable = object : AutoCloseable {
        val job: Job = scope.subscribe(store, onAction, onState)
        override fun close() = job.cancel()
    }

    public fun send(intent: I): Unit = store.send(intent)

    override fun close(): Unit = scope.cancel()
}
