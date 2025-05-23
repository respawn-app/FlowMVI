package pro.respawn.flowmvi.plugins.delegate

/**
 * Defines the mode of delegation between stores.
 * This interface determines how and when the delegate store's state is projected to the principal store.
 *
 * 1. [DelegationMode.Immediate] - make the state available immediately. This will force the delegated store to receive
 * a permanent subscription from the principal store.
 * 2. [DelegationMode.WhileSubscribed] - subscribe to the delegated using [pro.respawn.flowmvi.plugins.whileSubscribed]
 * plugin and update the projection of the [StoreDelegate] while the principal store has subscribers.
 *
 * @see StoreDelegate
 */
public sealed interface DelegationMode {
    /**
     * In this mode, the delegate store's state is directly projected to the principal store
     * and the delegate is subscribed to as soon as the principal store starts.
     *
     * @see StoreDelegate
     */
    public class Immediate : DelegationMode

    /**
     * In this mode, the delegate store's state is only projected to the principal store
     * when the principal store has subscribers.
     *
     * @property minSubs The minimum number of subscribers required for the delegation to be active.
     */
    public data class WhileSubscribed(val minSubs: Int = 1) : DelegationMode

    @Suppress("UndocumentedPublicClass") // fp
    public companion object {
        /**
         * The default delegation mode, which is [WhileSubscribed] with a minimum of 1 subscriber.
         */
        public val Default: DelegationMode = WhileSubscribed()
    }
}
