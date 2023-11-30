package pro.respawn.flowmvi.api

import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.cancellation.CancellationException

/**
 * An entity that can [recover] from exceptions happening during its lifecycle. Most often, a [Store]
 */
@Suppress("FUN_INTERFACE_WITH_SUSPEND_FUNCTION") // https://youtrack.jetbrains.com/issue/KTIJ-7642
public fun interface Recoverable<S : MVIState, I : MVIIntent, A : MVIAction> : CoroutineContext.Element {

    @OptIn(DelicateStoreApi::class)
    override val key: CoroutineContext.Key<*> get() = Recoverable

    /**
     * Recover from an exception in the given context.
     */
    public suspend fun PipelineContext<S, I, A>.recover(e: Exception)

    /**
     * Run [block] catching any exceptions and invoking [recover]. This will add this [Recoverable] key to the coroutine
     * context of the [recover] block.
     */
    @OptIn(DelicateStoreApi::class)
    public suspend fun PipelineContext<S, I, A>.catch(block: suspend () -> Unit): Unit = try {
        block()
    } catch (e: CancellationException) {
        throw e
    } catch (expected: Exception) {
        if (coroutineContext[Recoverable] != null) throw expected
        withContext(this@Recoverable) {
            recover(expected)
        }
    }

    @DelicateStoreApi
    public companion object : CoroutineContext.Key<Recoverable<*, *, *>>
}
