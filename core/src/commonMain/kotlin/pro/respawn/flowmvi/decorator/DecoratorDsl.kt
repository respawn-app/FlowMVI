package pro.respawn.flowmvi.decorator

import pro.respawn.flowmvi.annotation.ExperimentalFlowMVIAPI
import pro.respawn.flowmvi.api.FlowMVIDSL
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.PipelineContext
import pro.respawn.flowmvi.api.StorePlugin
import pro.respawn.flowmvi.api.context.ShutdownContext
import pro.respawn.flowmvi.api.context.UndeliveredHandlerContext
import pro.respawn.flowmvi.impl.plugin.PluginInstance
import pro.respawn.flowmvi.impl.plugin.asInstance

public typealias DecorateValue<S, I, A, V> = (
suspend PipelineContext<S, I, A>.(child: StorePlugin<S, I, A>, it: V) -> V?
)

public typealias DecorateState<S, I, A> = (
suspend PipelineContext<S, I, A>.(child: StorePlugin<S, I, A>, old: S, new: S) -> S?
)

public typealias Decorate<S, I, A> = (
suspend PipelineContext<S, I, A>.(child: StorePlugin<S, I, A>) -> Unit
)

public typealias DecorateArg<S, I, A, V> = (
suspend PipelineContext<S, I, A>.(child: StorePlugin<S, I, A>, it: V) -> Unit
)

public typealias DecorateOnStop<S, I, A> = (
ShutdownContext<S, I, A>.(child: StorePlugin<S, I, A>, e: Exception?) -> Unit
)

public typealias DecorateUndelivered<S, I, A, V> = (
UndeliveredHandlerContext<S, I, A>.(child: StorePlugin<S, I, A>, it: V) -> Unit
)

/**
 * Build a new [PluginDecorator].
 *
 * Consult the [PluginDecorator] and [DecoratorBuilder] docs for more info.
 */
@FlowMVIDSL
@ExperimentalFlowMVIAPI
public inline fun <S : MVIState, I : MVIIntent, A : MVIAction> decorator(
    block: DecoratorBuilder<S, I, A>.() -> Unit
): PluginDecorator<S, I, A> = DecoratorBuilder<S, I, A>().apply(block).build()

/**
 * Return a new [StorePlugin] that decorates `this` plugin using [decorator]
 */
@FlowMVIDSL
public infix fun <S : MVIState, I : MVIIntent, A : MVIAction> StorePlugin<S, I, A>.decoratedWith(
    decorator: PluginDecorator<S, I, A>
): StorePlugin<S, I, A> = asInstance().decorate(decorator)

/**
 * Returns a new [StorePlugin] with `this` [PluginDecorator] applied to the [plugin].
 */
@FlowMVIDSL
public infix fun <S : MVIState, I : MVIIntent, A : MVIAction> PluginDecorator<S, I, A>.decorates(
    plugin: StorePlugin<S, I, A>
): StorePlugin<S, I, A> = plugin.asInstance().decorate(this)

/**
 * Return a new [StorePlugin] that decorates `this` plugin using all [decorators].
 *
 * Decorators are wrapped in the order of iteration: `D_N( ...D1( D0( Plugin ) )... )`
 */
@FlowMVIDSL
public infix fun <S : MVIState, I : MVIIntent, A : MVIAction> StorePlugin<S, I, A>.decoratedWith(
    decorators: Iterable<PluginDecorator<S, I, A>>
): StorePlugin<S, I, A> = decorators.fold(this) { next, decorator -> next decoratedWith decorator }

internal infix fun <S : MVIState, I : MVIIntent, A : MVIAction> PluginInstance<S, I, A>.decorate(
    decorator: PluginDecorator<S, I, A>,
): PluginInstance<S, I, A> = copy(
    name = decorator.name,
    onState = wrapNotNull(onState, decorator.onState) { wrap ->
        ctx@{ old, new -> wrap(this, this@decorate, old, new) }
    },
    onIntent = wrapNotNull(onIntent, decorator.onIntent) { wrap ->
        ctx@{ wrap(this, this@decorate, it) }
    },
    onAction = wrapNotNull(onAction, decorator.onAction) { wrap ->
        ctx@{ wrap(this, this@decorate, it) }
    },
    onException = wrapNotNull(onException, decorator.onException) { wrap ->
        ctx@{ wrap(this, this@decorate, it) }
    },
    onStart = wrapNotNull(onStart, decorator.onStart) { wrap ->
        ctx@{ wrap(this, this@decorate) }
    },
    onSubscribe = wrapNotNull(onSubscribe, decorator.onSubscribe) { wrap ->
        ctx@{ wrap(this, this@decorate, it) }
    },
    onUnsubscribe = wrapNotNull(onUnsubscribe, decorator.onUnsubscribe) { wrap ->
        ctx@{ wrap(this, this@decorate, it) }
    },
    onStop = wrapNotNull(onStop, decorator.onStop) { wrap ->
        ctx@{ wrap(this, this@decorate, it) }
    },
    onUndeliveredIntent = wrapNotNull(onUndeliveredIntent, decorator.onUndeliveredIntent) { wrap ->
        ctx@{ wrap(this, this@decorate, it) }
    },
    onUndeliveredAction = wrapNotNull(onUndeliveredAction, decorator.onUndeliveredAction) { wrap ->
        ctx@{ wrap(this, this@decorate, it) }
    }
)

private inline fun <H, W> wrapNotNull(
    action: H?,
    wrapper: W?,
    transform: (block: W) -> H
): H? = action?.let { h -> wrapper?.let { w -> transform(w) } } ?: action
