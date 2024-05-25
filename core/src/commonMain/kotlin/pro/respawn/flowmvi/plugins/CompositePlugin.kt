package pro.respawn.flowmvi.plugins

import pro.respawn.flowmvi.api.FlowMVIDSL
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.StorePlugin
import pro.respawn.flowmvi.dsl.plugin
import pro.respawn.flowmvi.util.fastFold
import pro.respawn.flowmvi.util.fastForEach

/**
 * A plugin that delegates to [plugins] in the iteration order.
 * This is an implementation of the "Composite" pattern and the "Chain or Responsibility" pattern.
 *
 * This plugin is mostly not intended for usage in general code as there are no real use cases for it so far.
 * It can be useful in testing and custom store implementations.
 *
 * The [plugins] list must support random element access in order to be performant
 */
@FlowMVIDSL
public fun <S : MVIState, I : MVIIntent, A : MVIAction> compositePlugin(
    plugins: List<StorePlugin<S, I, A>>,
    name: String? = null,
): StorePlugin<S, I, A> = plugin {
    this.name = name
    onState { old: S, new: S -> plugins.iterate(new) { onState(old, it) } }
    onIntent { intent: I -> plugins.iterate(intent) { onIntent(it) } }
    onAction { action: A -> plugins.iterate(action) { onAction(it) } }
    onException { e: Exception -> plugins.iterate(e) { onException(it) } }
    onSubscribe { subs: Int -> plugins.iterate { onSubscribe(subs) } }
    onUnsubscribe { subs: Int -> plugins.iterate { onUnsubscribe(subs) } }
    onStart { plugins.iterate { onStart() } }
    onStop { plugins.iterate { onStop(it) } }
}

private inline fun <S : MVIState, I : MVIIntent, A : MVIAction> List<StorePlugin<S, I, A>>.iterate(
    block: StorePlugin<S, I, A>.() -> Unit,
) = fastForEach(block)

private inline fun <R, S : MVIState, I : MVIIntent, A : MVIAction> List<StorePlugin<S, I, A>>.iterate(
    initial: R,
    block: StorePlugin<S, I, A>.(R) -> R?
) = fastFold<_, R?>(initial) inner@{ acc, it -> block(it, acc ?: return@iterate acc) }
