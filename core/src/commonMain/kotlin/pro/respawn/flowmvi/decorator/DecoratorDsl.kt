package pro.respawn.flowmvi.decorator

import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.PipelineContext
import pro.respawn.flowmvi.impl.PluginInstance

public suspend inline fun <S : MVIState, I : MVIIntent, A : MVIAction> DecoratorContext<S, I, A, Unit>.proceed() {
    proceed(Unit)
}

private inline fun <S : MVIState, I : MVIIntent, A : MVIAction, R> PipelineContext<S, I, A>.context(
    crossinline block: suspend () -> R?
): DecoratorContext<S, I, A, R> = object : DecoratorContext<S, I, A, R>, PipelineContext<S, I, A> by this {

    override suspend fun proceed(with: R): R? = block()
}

@Suppress("Wrapping")
internal fun <S : MVIState, I : MVIIntent, A : MVIAction> PluginInstance<S, I, A>.decorate(
    decorator: StoreDecorator<S, I, A>,
): PluginInstance<S, I, A> = copy(
    name = decorator.name,
    onState = compose(onState, decorator.wrapState) { handler, wrapper ->
        { old, new -> decorator.run { context { handler(old, new) }.wrapper(old, new) } }
    },
    onIntent = compose(onIntent, decorator.wrapIntent) { handler, wrapper ->
        { decorator.run { context { handler(it) }.wrapper(it) } }
    },
    onAction = compose(onAction, decorator.wrapAction) { handler, wrapper ->
        { decorator.run { context { handler(it) }.wrapper(it) } }
    },
    onException = compose(onException, decorator.wrapException) { handler, wrapper ->
        { decorator.run { context { handler(it) }.wrapper(it) } }
    },
    onStart = compose(onStart, decorator.wrapStart) { handler, wrapper ->
        { decorator.run { context { handler() }.wrapper() } }
    },
    onSubscribe = compose(onSubscribe, decorator.wrapSubscribe) { handler, wrapper ->
        { decorator.run { context { handler(it) }.wrapper() } }
    },
    onUnsubscribe = compose(onUnsubscribe, decorator.wrapUnsubscribe) { handler, wrapper ->
        { decorator.run { context { handler(it) }.wrapper() } }
    },
)

private inline fun <H, W, R> compose(
    handler: H?,
    wrapper: W?,
    block: (handler: H, wrapper: W) -> R
) = handler?.let { h -> wrapper?.let { w -> block(h, w) } }
