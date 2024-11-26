package pro.respawn.flowmvi.decorator

import pro.respawn.flowmvi.annotation.ExperimentalFlowMVIAPI
import pro.respawn.flowmvi.api.FlowMVIDSL
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.StorePlugin
import pro.respawn.flowmvi.impl.decorator.asInstance
import pro.respawn.flowmvi.impl.decorator.decorate
import pro.respawn.flowmvi.impl.plugin.asInstance

public suspend inline fun <S : MVIState, I : MVIIntent, A : MVIAction> DecoratorContext<S, I, A, Unit>.proceed() {
    proceed(Unit)
}

public suspend inline fun <S : MVIState, I : MVIIntent, A : MVIAction, T> DecoratorContext<S, I, A, T>.ignore(): T? =
    proceed(null)

@FlowMVIDSL
@ExperimentalFlowMVIAPI
public inline fun <S : MVIState, I : MVIIntent, A : MVIAction> decorator(
    block: DecoratorBuilder<S, I, A>.() -> Unit
): PluginDecorator<S, I, A> = DecoratorBuilder<S, I, A>().apply(block).build()

@FlowMVIDSL
public infix fun <S : MVIState, I : MVIIntent, A : MVIAction> StorePlugin<S, I, A>.decoratedWith(
    decorator: PluginDecorator<S, I, A>
): StorePlugin<S, I, A> = asInstance().decorate(decorator.asInstance())

@FlowMVIDSL
public infix fun <S : MVIState, I : MVIIntent, A : MVIAction> PluginDecorator<S, I, A>.decorates(
    plugin: StorePlugin<S, I, A>
): StorePlugin<S, I, A> = plugin.asInstance().decorate(asInstance())

@FlowMVIDSL
public infix fun <S : MVIState, I : MVIIntent, A : MVIAction> StorePlugin<S, I, A>.decoratedWith(
    decorators: Iterable<PluginDecorator<S, I, A>>
): StorePlugin<S, I, A> = decorators.fold(this) { next, decorator -> next decoratedWith decorator }
