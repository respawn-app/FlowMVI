package pro.respawn.flowmvi.modules

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import pro.respawn.flowmvi.api.IntentReceiver
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.PipelineContext
import pro.respawn.flowmvi.api.StoreConfiguration
import pro.respawn.flowmvi.exceptions.UnhandledIntentException
import pro.respawn.flowmvi.util.wrap

internal interface IntentModule<S : MVIState, I : MVIIntent, A : MVIAction> : IntentReceiver<I> {

    suspend fun PipelineContext<S, I, A>.reduceForever()
}

@Suppress("UNCHECKED_CAST")
internal fun <S : MVIState, I : MVIIntent, A : MVIAction> intentModule(
    config: StoreConfiguration<S>,
    onUndelivered: ((intent: I) -> Unit)?,
    onEnqueue: ((intent: I) -> I?)?,
    onIntent: (suspend PipelineContext<S, I, A>.(intent: I) -> I?)?,
): IntentModule<S, I, A> = when {
    onIntent == null -> NoOpIntentModule(config.debuggable)
    !config.parallelIntents -> SequentialChannelIntentModule(
        capacity = config.intentCapacity,
        overflow = config.onOverflow,
        onUndelivered = onUndelivered,
        onEnqueue = onEnqueue,
        onIntent = onIntent,
        debuggable = config.debuggable
    )
    else -> ParallelChannelIntentModule(
        capacity = config.intentCapacity,
        overflow = config.onOverflow,
        onUndelivered = onUndelivered,
        onEnqueue = onEnqueue,
        onIntent = onIntent,
        debuggable = config.debuggable
    )
}

private class NoOpIntentModule<S : MVIState, I : MVIIntent, A : MVIAction>(
    private val debuggable: Boolean
) : IntentModule<S, I, A> {

    override suspend fun PipelineContext<S, I, A>.reduceForever() = Unit
    override suspend fun emit(intent: I) = intent(intent)
    override fun intent(intent: I) = unhandled(intent, debuggable)
}

private abstract class ChannelIntentModule<S : MVIState, I : MVIIntent, A : MVIAction>(
    capacity: Int,
    overflow: BufferOverflow,
    private val onUndelivered: ((intent: I) -> Unit)?,
    private val onEnqueue: ((intent: I) -> I?)?,
    private val onIntent: suspend PipelineContext<S, I, A>.(intent: I) -> I?,
    private val debuggable: Boolean,
) : IntentModule<S, I, A> {

    val intents = Channel(capacity, overflow, onUndelivered)

    abstract suspend fun PipelineContext<S, I, A>.dispatch(intent: I)

    protected suspend inline fun PipelineContext<S, I, A>.reduce(intent: I) {
        val unhandled = onIntent(intent) ?: return
        onUndelivered?.invoke(unhandled) ?: unhandled(unhandled, debuggable)
    }

    override suspend fun emit(intent: I) = wrap(intent, onEnqueue) { intent -> intents.send(intent) }

    override fun intent(intent: I): Unit = wrap(intent, onEnqueue) { intent ->
        if (intents.trySend(intent).isSuccess) return
        onUndelivered?.invoke(intent)
    }

    override suspend fun PipelineContext<S, I, A>.reduceForever() {
        while (isActive) dispatch(intents.receive())
    }
}

private class SequentialChannelIntentModule<S : MVIState, I : MVIIntent, A : MVIAction>(
    capacity: Int,
    overflow: BufferOverflow,
    onUndelivered: ((intent: I) -> Unit)?,
    onEnqueue: ((intent: I) -> I?)?,
    onIntent: suspend PipelineContext<S, I, A>.(intent: I) -> I?,
    debuggable: Boolean,
) : ChannelIntentModule<S, I, A>(capacity, overflow, onUndelivered, onEnqueue, onIntent, debuggable) {

    override suspend fun PipelineContext<S, I, A>.dispatch(intent: I) = reduce(intent)
}

private class ParallelChannelIntentModule<S : MVIState, I : MVIIntent, A : MVIAction>(
    capacity: Int,
    overflow: BufferOverflow,
    onUndelivered: ((intent: I) -> Unit)?,
    onEnqueue: ((intent: I) -> I?)?,
    onIntent: suspend PipelineContext<S, I, A>.(intent: I) -> I?,
    debuggable: Boolean,
) : ChannelIntentModule<S, I, A>(capacity, overflow, onUndelivered, onEnqueue, onIntent, debuggable) {

    override suspend fun PipelineContext<S, I, A>.dispatch(intent: I) {
        launch { reduce(intent) }
    }
}

private inline fun <I : MVIIntent> unhandled(intent: I, debuggable: Boolean) {
    if (debuggable) throw UnhandledIntentException(intent)
}
