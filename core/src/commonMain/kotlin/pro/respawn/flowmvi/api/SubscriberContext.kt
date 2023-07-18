package pro.respawn.flowmvi.api

import kotlinx.coroutines.CoroutineScope

public class SubscriberContext<S : MVIState, I : MVIIntent, A : MVIAction>(
    private val provider: Provider<S, I, A>,
    private val scope: CoroutineScope
) : CoroutineScope by scope, Provider<S, I, A> by provider
