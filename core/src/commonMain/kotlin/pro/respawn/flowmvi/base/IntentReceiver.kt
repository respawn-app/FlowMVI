package pro.respawn.flowmvi.base

import pro.respawn.flowmvi.MVIIntent

public fun interface IntentReceiver<in I : MVIIntent> {

    /**
     * Send an intent asynchronously
     * @See MVIIntent
     */
    public fun send(intent: I)
}
