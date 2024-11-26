package pro.respawn.flowmvi.decorator

import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.impl.plugin.PluginInstance

@Suppress("Wrapping")
internal fun <S : MVIState, I : MVIIntent, A : MVIAction> PluginInstance<S, I, A>.decorate(
    decorator: PluginDecorator<S, I, A>,
): PluginInstance<S, I, A> = copy(
    name = decorator.name,
    onState = wrapNotNull(onState, decorator.onState) { wrap ->
        { old, new -> wrap(this, this@decorate, old, new) }
    },
    onIntent = wrapNotNull(onIntent, decorator.onIntent) { wrap ->
        { wrap(this, this@decorate, it) }
    },
    onAction = wrapNotNull(onAction, decorator.onAction) { wrap ->
        { wrap(this, this@decorate, it) }
    },
    onException = wrapNotNull(onException, decorator.onException) { wrap ->
        { wrap(this, this@decorate, it) }
    },
    onStart = wrapNotNull(onStart, decorator.onStart) { wrap ->
        { wrap(this, this@decorate) }
    },
    onSubscribe = wrapNotNull(onSubscribe, decorator.onSubscribe) { wrap ->
        { wrap(this, this@decorate, it) }
    },
    onUnsubscribe = wrapNotNull(onUnsubscribe, decorator.onUnsubscribe) { wrap ->
        { wrap(this, this@decorate, it) }
    },
)

private inline fun <H, W> wrapNotNull(
    action: H?,
    wrapper: W?,
    transform: (block: W) -> H
): H? = action?.let { h -> wrapper?.let { w -> transform(w) } } ?: action
