package pro.respawn.flowmvi.impl.decorator

import pro.respawn.flowmvi.annotation.NotIntendedForInheritance
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.PipelineContext
import pro.respawn.flowmvi.decorator.DecoratorContext
import pro.respawn.flowmvi.decorator.StoreDecorator
import pro.respawn.flowmvi.impl.plugin.PluginInstance
import pro.respawn.flowmvi.util.typed

@Suppress("Wrapping")
internal fun <S : MVIState, I : MVIIntent, A : MVIAction> PluginInstance<S, I, A>.decorate(
    decorator: DecoratorInstance<S, I, A>,
): PluginInstance<S, I, A> = copy(
    name = decorator.name,
    onState = compose(onState, decorator.onState) { handler, wrapper ->
        { old, new -> context { handler(old, new) }.wrapper(old, new) }
    },
    onIntent = compose(onIntent, decorator.onIntent) { handler, wrapper ->
        { context { handler(it) }.wrapper(it) }
    },
    onAction = compose(onAction, decorator.onAction) { handler, wrapper ->
        { context { handler(it) }.wrapper(it) }
    },
    onException = compose(onException, decorator.onException) { handler, wrapper ->
        { context { handler(it) }.wrapper(it) }
    },
    onStart = compose(onStart, decorator.onStart) { handler, wrapper ->
        { context { handler() }.wrapper() }
    },
    onSubscribe = compose(onSubscribe, decorator.onSubscribe) { handler, wrapper ->
        { context { handler(it) }.wrapper(it) }
    },
    onUnsubscribe = compose(onUnsubscribe, decorator.onUnsubscribe) { handler, wrapper ->
        { context { handler(it) }.wrapper(it) }
    },
)

internal fun <S : MVIState, I : MVIIntent, A : MVIAction> StoreDecorator<S, I, A>.asInstance() =
    typed<DecoratorInstance<S, I, A>>() ?: DecoratorInstance(
        name = name,
        onState = { old, new -> onState(old, new) },
        onIntent = { intent -> onIntent(intent) },
        onAction = { action -> onAction(action) },
        onStart = { onStart() },
        onSubscribe = { onSubscribe(it) },
        onUnsubscribe = { onUnsubscribe(it) },
        onException = { e -> onException(e) },
    )

@OptIn(NotIntendedForInheritance::class)
private inline fun <S : MVIState, I : MVIIntent, A : MVIAction, R> PipelineContext<S, I, A>.context(
    crossinline block: suspend () -> R?
): DecoratorContext<S, I, A, R> = object : DecoratorContext<S, I, A, R>, PipelineContext<S, I, A> by this {

    override suspend fun proceed(with: R): R? = block()
}

private inline fun <H, W, R> compose(
    handler: H?,
    wrapper: W?,
    block: (handler: H, wrapper: W) -> R
) = handler?.let { h -> wrapper?.let { w -> block(h, w) } }
