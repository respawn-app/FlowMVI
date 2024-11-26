package pro.respawn.flowmvi.impl.decorator

import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.decorator.PluginDecorator
import pro.respawn.flowmvi.impl.plugin.PluginInstance
import pro.respawn.flowmvi.util.typed

@Suppress("Wrapping")
internal fun <S : MVIState, I : MVIIntent, A : MVIAction> PluginInstance<S, I, A>.decorate(
    decorator: DecoratorInstance<S, I, A>,
): PluginInstance<S, I, A> = copy(
    name = decorator.name,
    onState = wrapNotNull(onState, decorator.onState) { proceed, wrap ->
        { old, new -> withContext(this@decorate, decorator, { proceed(old, new) }) { wrap(old, new) } }
    },
    onIntent = wrapNotNull(onIntent, decorator.onIntent) { proceed, wrap ->
        { withContext(this@decorate, decorator, { proceed(it) }) { wrap(it) } }
    },
    onAction = wrapNotNull(onAction, decorator.onAction) { proceed, wrap ->
        { withContext(this@decorate, decorator, { proceed(it) }) { wrap(it) } }
    },
    onException = wrapNotNull(onException, decorator.onException) { proceed, wrap ->
        { withContext(this@decorate, decorator, { proceed(it) }) { wrap(it) } }
    },
    onStart = wrapNotNull(onStart, decorator.onStart) { proceed, wrap ->
        { withContext(this@decorate, decorator, { proceed() }) { wrap() } }
    },
    onSubscribe = wrapNotNull(onSubscribe, decorator.onSubscribe) { proceed, wrap ->
        { subs -> withContext(this@decorate, decorator, { proceed(it); subs }) { wrap(subs) } }
    },
    onUnsubscribe = wrapNotNull(onUnsubscribe, decorator.onUnsubscribe) { proceed, wrap ->
        { subs -> withContext(this@decorate, decorator, { proceed(it); subs }) { wrap(subs) } }
    },
)

internal fun <S : MVIState, I : MVIIntent, A : MVIAction> PluginDecorator<S, I, A>.asInstance() =
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

private inline fun <H, W> wrapNotNull(
    action: H?,
    wrapper: W?,
    transform: (handler: H, wrapper: W) -> H
): H? = action?.let { h -> wrapper?.let { w -> transform(h, w) } } ?: action
