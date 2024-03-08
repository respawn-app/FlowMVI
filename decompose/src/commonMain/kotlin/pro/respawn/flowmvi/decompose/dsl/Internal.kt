package pro.respawn.flowmvi.decompose.dsl

import kotlinx.coroutines.CoroutineScope
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.Store
import pro.respawn.flowmvi.decompose.api.RetainedStore

@PublishedApi
internal fun <S : MVIState, I : MVIIntent, A : MVIAction> retained(
    store: Store<S, I, A>,
    scope: CoroutineScope?,
): RetainedStore<S, I, A> = object : Store<S, I, A> by store, RetainedStore<S, I, A> {
    init {
        if (scope != null) start(scope)
    }
}

@PublishedApi
internal fun <S : MVIState, I : MVIIntent, A : MVIAction> Store<S, I, A>.retain(
    scope: CoroutineScope?
): RetainedStore<S, I, A> = retained(this, scope)
