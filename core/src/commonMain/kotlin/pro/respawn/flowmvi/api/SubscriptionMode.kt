package pro.respawn.flowmvi.api

/**
 * Subscription mode of the UI element with a dedicated lifecycle.
 * An implementation of the [SubscriberLifecycle] must follow the contract outlined for each mode.
 */
public enum class SubscriptionMode {

    /**
     * Subscribe using a composable function's lifecycle without considering if the UI is fully or partially visible.
     */
    Immediate,

    /**
     * Subscribe when the UI is visible, but may be covered by other elements such as Dialogs, modals, pop-ups etc.
     * Should unsubscribe when the composable may still be alive but is no longer fully or partially visible.
     *
     * On Android, corresponds to the `onStart` activity lifecycle callback,
     * thus the mode is no longer active in `onStop`.
     */
    Started,

    /**
     * Subscribe when the UI is fully visible and is not fully or partially covered by anything.
     *
     * A [SubscriberLifecycle] following this mode must unsubscribe when the composable is still in the composition,
     * but is fully or **partially** covered by something else, such as modals, pop-ups, windows etc.
     *
     * On Android, corresponds to the `onResume` lifecycle event, with the mode being no longer active in `onPause`.
     */
    Visible,
}
