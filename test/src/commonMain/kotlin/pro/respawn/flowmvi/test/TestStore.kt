package pro.respawn.flowmvi.test

import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.Store
import pro.respawn.flowmvi.api.context.SubscriptionAware

public interface TestStore<S : MVIState, I : MVIIntent, A : MVIAction> : Store<S, I, A>, SubscriptionAware

/**
 * We don't want to expose `SubscriptionAware` to store public api, but want it in tests.
 *
 * Keep the cast in one place to minimize migration overhead if the interface changes.
 */
@PublishedApi
internal fun <S : MVIState, I : MVIIntent, A : MVIAction> TestStore(
    store: Store<S, I, A>,
): TestStore<S, I, A> = object :
    TestStore<S, I, A>,
    Store<S, I, A> by store,
    SubscriptionAware by (store as SubscriptionAware) {}
