package pro.respawn.flowmvi.impl

import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.PipelineContext
import pro.respawn.flowmvi.api.StorePlugin
import pro.respawn.flowmvi.api.context.ShutdownContext
import pro.respawn.flowmvi.api.context.UndeliveredHandlerContext

/**
 * Optimized plugin implementation that stores optional lambdas directly and avoids their invocations
 */
internal class PluginInstance<S : MVIState, I : MVIIntent, A : MVIAction>(
    val onState: (suspend PipelineContext<S, I, A>.(old: S, new: S) -> S?)? = null,
    val onIntent: (suspend PipelineContext<S, I, A>.(intent: I) -> I?)? = null,
    val onAction: (suspend PipelineContext<S, I, A>.(action: A) -> A?)? = null,
    val onException: (suspend PipelineContext<S, I, A>.(e: Exception) -> Exception?)? = null,
    val onStart: (suspend PipelineContext<S, I, A>.() -> Unit)? = null,
    val onSubscribe: (suspend PipelineContext<S, I, A>.(subscriberCount: Int) -> Unit)? = null,
    val onUnsubscribe: (suspend PipelineContext<S, I, A>.(subscriberCount: Int) -> Unit)? = null,
    val onStop: (ShutdownContext<S, I, A>.(e: Exception?) -> Unit)? = null,
    val onUndeliveredIntent: (UndeliveredHandlerContext<S, I, A>.(intent: I) -> Unit)? = null,
    val onUndeliveredAction: (UndeliveredHandlerContext<S, I, A>.(action: A) -> Unit)? = null,
    override val name: String? = null,
) : StorePlugin<S, I, A> {

    override suspend fun PipelineContext<S, I, A>.onState(old: S, new: S): S? {
        return (onState ?: return new).invoke(this, old, new)
    }

    override suspend fun PipelineContext<S, I, A>.onIntent(intent: I): I? {
        return (onIntent ?: return intent).invoke(this, intent)
    }

    override suspend fun PipelineContext<S, I, A>.onAction(action: A): A? {
        return (onAction ?: return action).invoke(this, action)
    }

    override suspend fun PipelineContext<S, I, A>.onException(e: Exception): Exception? {
        return (onException ?: return e).invoke(this, e)
    }

    override suspend fun PipelineContext<S, I, A>.onStart() {
        onStart?.invoke(this)
    }

    override suspend fun PipelineContext<S, I, A>.onSubscribe(newSubscriberCount: Int) {
        onSubscribe?.invoke(this, newSubscriberCount)
    }

    override suspend fun PipelineContext<S, I, A>.onUnsubscribe(newSubscriberCount: Int) {
        onUnsubscribe?.invoke(this, newSubscriberCount)
    }

    override fun ShutdownContext<S, I, A>.onStop(e: Exception?) {
        onStop?.invoke(this, e)
    }

    override fun UndeliveredHandlerContext<S, I, A>.onUndeliveredIntent(intent: I) {
        onUndeliveredIntent?.invoke(this, intent)
    }

    override fun UndeliveredHandlerContext<S, I, A>.onUndeliveredAction(action: A) {
        onUndeliveredAction?.invoke(this, action)
    }

    override fun toString(): String = "StorePlugin${name?.let { " \"$it\"" }.orEmpty()}"
    override fun hashCode(): Int = name?.hashCode() ?: super.hashCode()
    override fun equals(other: Any?): Boolean = when {
        other !is StorePlugin<*, *, *> -> false
        other.name == null && name == null -> this === other
        else -> name == other.name
    }
}
