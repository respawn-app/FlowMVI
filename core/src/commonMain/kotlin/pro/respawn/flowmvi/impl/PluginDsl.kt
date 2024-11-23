package pro.respawn.flowmvi.impl

import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.StorePlugin

internal fun <S : MVIState, I : MVIIntent, A : MVIAction> StorePlugin<S, I, A>.asInstance(
) = this as? PluginInstance ?: PluginInstance(
    onState = { old: S, new: S -> onState(old, new) },
    onIntent = { intent: I -> onIntent(intent) },
    onAction = { action: A -> onAction(action) },
    onException = { e -> onException(e) },
    onStart = { onStart() },
    onSubscribe = { subscriberCount -> onSubscribe(subscriberCount) },
    onUnsubscribe = { subscriberCount -> onUnsubscribe(subscriberCount) },
    onStop = { e -> onStop(e) },
    onUndeliveredIntent = { intent -> onUndeliveredIntent(intent) },
)
