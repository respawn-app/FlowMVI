package pro.respawn.flowmvi.api

public interface IntentReceiver<in I : MVIIntent> {

    /**
     * Send an intent asynchronously
     * @See MVIIntent
     */
    public fun send(intent: I)
    public suspend fun emit(intent: I)
    public fun intent(intent: I): Unit = send(intent)
}
