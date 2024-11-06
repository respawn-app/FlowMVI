package pro.respawn.flowmvi.plugins

import pro.respawn.flowmvi.api.FlowMVIDSL
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.StorePlugin
import pro.respawn.flowmvi.dsl.StorePlugin
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
): StorePlugin<S, I, A> = StorePlugin(
    name = name,
    onState = { old: S, new: S -> plugins.chain(new) { next -> onState(old, next) } },
    onIntent = { intent: I -> plugins.chain(intent) { onIntent(it) } },
    onAction = { action: A -> plugins.chain(action) { onAction(it) } },
    onException = { e: Exception -> plugins.chainException(e) { onException(it) } },
    onSubscribe = { subs: Int -> plugins.chain { onSubscribe(subs) } },
    onUnsubscribe = { subs: Int -> plugins.chain { onUnsubscribe(subs) } },
    onStart = { plugins.chain { onStart() } },
    onStop = { plugins.asReversed().chain { onStop(it) } }
)

private inline fun <S : MVIState, I : MVIIntent, A : MVIAction> List<StorePlugin<S, I, A>>.chain(
    block: StorePlugin<S, I, A>.() -> Unit,
) = fastForEach(block)

private inline fun <R, S : MVIState, I : MVIIntent, A : MVIAction> List<StorePlugin<S, I, A>>.chain(
    initial: R,
    block: StorePlugin<S, I, A>.(R) -> R?
) = fastFold<_, R?>(initial) inner@{ acc, it -> block(it, acc ?: return@chain acc) }

// TODO: https://youtrack.jetbrains.com/issue/KT-68509
private inline fun <S : MVIState, I : MVIIntent, A : MVIAction> List<StorePlugin<S, I, A>>.chainException(
    initial: Exception,
    block: StorePlugin<S, I, A>.(Exception) -> Exception?
) = fastFold<_, Exception?>(initial) inner@{ acc, it -> block(it, acc ?: return@chainException acc) }
