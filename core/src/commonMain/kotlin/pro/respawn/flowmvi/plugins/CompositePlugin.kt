package pro.respawn.flowmvi.plugins

import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.StorePlugin
import pro.respawn.flowmvi.dsl.plugin

/**
 * A plugin that delegates to [plugins] in the iteration order.
 * This is an implementation of the "Composite" pattern and the "Chain or Responsibility" pattern.
 *
 * This plugin is mostly not intended for usage in general code as there are no real use cases for it so far.
 * It can be useful in testing and custom store implementations.
 */
public fun <S : MVIState, I : MVIIntent, A : MVIAction> compositePlugin(
    plugins: Set<StorePlugin<S, I, A>>,
    name: String? = null,
): StorePlugin<S, I, A> = plugin {
    this.name = name
    onState { old: S, new: S -> plugins.fold(new) { onState(old, it) } }
    onIntent { intent: I -> plugins.fold(intent) { onIntent(it) } }
    onAction { action: A -> plugins.fold(action) { onAction(it) } }
    onException { e: Exception -> plugins.fold(e) { onException(it) } }
    onUnsubscribe { subs: Int -> plugins.fold { onUnsubscribe(subs) } }
    onSubscribe { subs: Int -> plugins.fold { onSubscribe(subs) } }
    onStart { plugins.fold { onStart() } }
    onStop { plugins.fold { onStop(it) } }
}

private inline fun <S : MVIState, I : MVIIntent, A : MVIAction> Iterable<StorePlugin<S, I, A>>.fold(
    block: StorePlugin<S, I, A>.() -> Unit,
) = forEach(block)

private inline fun <R, S : MVIState, I : MVIIntent, A : MVIAction> Iterable<StorePlugin<S, I, A>>.fold(
    initial: R,
    block: StorePlugin<S, I, A>.(R) -> R?
) = fold<_, R?>(initial) inner@{ acc, it -> it.block(acc ?: return@fold acc) }
