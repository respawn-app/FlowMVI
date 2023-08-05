package pro.respawn.flowmvi.api

import kotlin.coroutines.CoroutineContext

/**
 * An entity that can [recover] from exceptions happening during its lifecycle. Most often, a [Store]
 */
@Suppress("FUN_INTERFACE_WITH_SUSPEND_FUNCTION") // https://youtrack.jetbrains.com/issue/KTIJ-7642
@DelicateStoreApi
public fun interface Recoverable<S : MVIState, I : MVIIntent, A : MVIAction> : CoroutineContext.Element {

    override val key: CoroutineContext.Key<*> get() = Recoverable

    /**
     * Recover from an exception in the given context.
     */
    public suspend fun PipelineContext<S, I, A>.recover(e: Exception)

    @DelicateStoreApi
    public companion object : CoroutineContext.Key<Recoverable<*, *, *>>
}
