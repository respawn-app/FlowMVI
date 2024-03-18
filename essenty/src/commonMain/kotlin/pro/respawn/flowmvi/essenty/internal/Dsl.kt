package pro.respawn.flowmvi.essenty.internal

import kotlinx.coroutines.CoroutineScope
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.Store

@PublishedApi
internal fun <S : MVIState, I : MVIIntent, A : MVIAction> retained(
    store: Store<S, I, A>,
    scope: CoroutineScope?,
): RetainedStore<S, I, A> = object : Store<S, I, A> by store, RetainedStore<S, I, A> {
    init {
        if (scope != null) start(scope)
    }
}
