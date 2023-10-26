package pro.respawn.flowmvi.api

import androidx.compose.runtime.Stable
import kotlinx.coroutines.flow.Flow

/**
 * A central business logic unit for handling [MVIIntent]s, [MVIAction]s, and [MVIState]s.
 * Usually not subclassed but used with a corresponding builder (see [pro.respawn.flowmvi.dsl.store]).
 * A store functions independently of any subscribers, has its own lifecycle, can be stopped and relaunched at will.
 * The store can be mutated only through [MVIIntent].
 * Store is an [IntentReceiver] and can be [close]d to stop it.
 */
@Stable
public interface Store<out S : MVIState, in I : MVIIntent, out A : MVIAction> :
    ImmutableStore<S, I, A>,
    IntentReceiver<I> {

    /**
     * Obtain the current state in an unsafe manner.
     * This property is not thread-safe and parallel state updates will introduce a race condition when not
     * handled properly.
     * Such race conditions arise when using multiple data streams such as [Flow]s.
     *
     * Accessing and modifying the state this way will **circumvent ALL plugins** and will not make state updates atomic.
     */
    @DelicateStoreApi
    public val state: S
}
