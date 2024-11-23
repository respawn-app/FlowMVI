package pro.respawn.flowmvi.impl

import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.util.fastFold
import pro.respawn.flowmvi.util.fastForEach

@Suppress("Wrapping") // broken formatting in detekt
internal fun <S : MVIState, I : MVIIntent, A : MVIAction> List<PluginInstance<S, I, A>>.compose(
    name: String? = null,
) = PluginInstance(
    name = name,
    onStart = compose(PluginInstance<S, I, A>::onStart) {
        ctx@{ fastForEach { it(this@ctx) } }
    },
    onStop = compose(PluginInstance<S, I, A>::onStop) {
        ctx@{ e -> asReversed().fastForEach { it(this@ctx, e) } }
    },
    onUndeliveredIntent = compose(PluginInstance<S, I, A>::onUndeliveredIntent) {
        ctx@{ intent -> fastForEach { it(this@ctx, intent) } }
    },
    onState = compose(PluginInstance<S, I, A>::onState) {
        ctx@{ old: S, new: S -> curry(new) { next -> invoke(this@ctx, old, next) } }
    },
    onIntent = compose(PluginInstance<S, I, A>::onIntent) {
        ctx@{ initial: I -> curry(initial) { next -> invoke(this@ctx, next) } }
    },
    onAction = compose(PluginInstance<S, I, A>::onAction) {
        ctx@{ initial: A -> curry(initial) { next -> invoke(this@ctx, next) } }
    },
    onException = compose(PluginInstance<S, I, A>::onException) {
        ctx@{ e: Exception -> curry(e) { next -> invoke(this@ctx, next) } }
    },
    onSubscribe = compose(PluginInstance<S, I, A>::onSubscribe) {
        ctx@{ subs: Int -> fastForEach { it(this@ctx, subs) } }
    },
    onUnsubscribe = compose(PluginInstance<S, I, A>::onUnsubscribe) {
        ctx@{ subs: Int -> fastForEach { it(this@ctx, subs) } }
    },
)

private inline fun <A : MVIAction, I : MVIIntent, S : MVIState, L, R> List<PluginInstance<S, I, A>>.compose(
    @BuilderInference selector: PluginInstance<S, I, A>.() -> L?,
    @BuilderInference block: List<L>.() -> R,
): R? = mapNotNull(selector).takeIf { it.isNotEmpty() }?.block()

private inline fun <R, L> List<L>.curry(
    initial: R,
    block: L.(R) -> R?
): R? = fastFold<_, R?>(initial) inner@{ acc, it -> block(it, acc ?: return@curry acc) }
