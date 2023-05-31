package pro.respawn.flowmvi.action

import pro.respawn.flowmvi.MVIAction
import pro.respawn.flowmvi.MVIProvider

public fun interface ActionReceiver<in A : MVIAction> {

    /**
     * Send a new side-effect to be processed by subscribers, only once.
     * Actions not consumed will await in the queue with max capacity given as an implementation detail.
     * Actions that make the capacity overflow will be dropped, starting with the oldest.
     * How actions will be distributed depends on [ActionShareBehavior].
     * @See MVIProvider
     */
    public fun send(action: A)
}
