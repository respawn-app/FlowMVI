package pro.respawn.flowmvi.api

public fun interface IntentReceiver<in I : MVIIntent> {

    /**
     * Send an intent asynchronously
     * @See MVIIntent
     */
    public fun send(intent: I)
}
