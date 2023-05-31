package pro.respawn.flowmvi.action

import pro.respawn.flowmvi.MVIAction
import pro.respawn.flowmvi.MVIIntent
import pro.respawn.flowmvi.MVIState
import pro.respawn.flowmvi.store.Store

public interface Store<S : MVIState, in I : MVIIntent, A : MVIAction> :
    Store<S, I>,
    ActionProvider<A>,
    ActionReceiver<A>
