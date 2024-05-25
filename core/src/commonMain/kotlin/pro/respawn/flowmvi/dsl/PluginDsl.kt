package pro.respawn.flowmvi.dsl

import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.PipelineContext
import pro.respawn.flowmvi.api.StorePlugin

/**
 * Create a new instance of [StorePlugin] using provided callback parameters.
 *
 * See [plugin] for a DSL-like experience.
 */
internal inline fun <S : MVIState, I : MVIIntent, A : MVIAction> StorePlugin(
    @BuilderInference crossinline onState: suspend PipelineContext<S, I, A>.(old: S, new: S) -> S? = { _, new -> new },
    @BuilderInference crossinline onIntent: suspend PipelineContext<S, I, A>.(intent: I) -> I? = { it },
    @BuilderInference crossinline onAction: suspend PipelineContext<S, I, A>.(action: A) -> A? = { it },
    @BuilderInference crossinline onException: suspend PipelineContext<S, I, A>.(e: Exception) -> Exception? = { it },
    @BuilderInference crossinline onStart: suspend PipelineContext<S, I, A>.() -> Unit = {},
    @BuilderInference crossinline onSubscribe: suspend PipelineContext<S, I, A>.(subs: Int) -> Unit = {},
    @BuilderInference crossinline onUnsubscribe: suspend PipelineContext<S, I, A>.(subs: Int) -> Unit = {},
    @BuilderInference crossinline onStop: (e: Exception?) -> Unit = {},
    name: String? = null,
): StorePlugin<S, I, A> = object : StorePlugin<S, I, A> {

    override val name = name

    override suspend fun PipelineContext<S, I, A>.onStart() = onStart(this)
    override suspend fun PipelineContext<S, I, A>.onState(old: S, new: S) = onState(this, old, new)
    override suspend fun PipelineContext<S, I, A>.onIntent(intent: I) = onIntent(this, intent)
    override suspend fun PipelineContext<S, I, A>.onAction(action: A) = onAction(this, action)
    override suspend fun PipelineContext<S, I, A>.onException(e: Exception) = onException(this, e)
    override fun onStop(e: Exception?) = onStop.invoke(e)

    override suspend fun PipelineContext<S, I, A>.onSubscribe(
        newSubscriberCount: Int
    ) = onSubscribe(this, newSubscriberCount)

    override suspend fun PipelineContext<S, I, A>.onUnsubscribe(
        newSubscriberCount: Int
    ) = onUnsubscribe(this, newSubscriberCount)

    override fun toString(): String = "StorePlugin${name?.let { " \"$it\"" }.orEmpty()}"
    override fun hashCode(): Int = name?.hashCode() ?: super.hashCode()
    override fun equals(other: Any?): Boolean = when {
        other !is StorePlugin<*, *, *> -> false
        other.name == null && name == null -> this === other
        else -> name == other.name
    }
}
