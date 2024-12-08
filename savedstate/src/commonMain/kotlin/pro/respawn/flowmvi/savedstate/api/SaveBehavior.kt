package pro.respawn.flowmvi.savedstate.api

import pro.respawn.flowmvi.savedstate.plugins.saveStatePlugin
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * An interface that specifies **when** [saveStatePlugin] is going to save the store's state.
 * Multiple variants can be used, but there are restrictions on how you can combine them
 * (see [saveStatePlugin] documentation for more details).
 */
public sealed interface SaveBehavior {

    /**
     * A saving behavior that saves the state when after it has changed.
     * The newest state will be saved. (i.e. the state the store has after the [delay] has passed.
     * The delay will be reset and previous state save job will be canceled if state changes again.
     *
     * This effectively "throttles" the saving as the user changes the state.
     *
     * When multiple [OnChange] are provided, the **minimum** delay across all of them will be used.
     * @see [SaveBehavior]
     * @see [saveStatePlugin]
     */
    public data class OnChange(val delay: Duration = DefaultDelayMs.milliseconds) : SaveBehavior

    /**
     * A saving behavior that saves the state when [remainingSubscribers] count drops below the specified amount.
     * * By default, `0` is used.
     * * If you specify multiple [remainingSubscribers] values, the **maximum** value will be used.
     * * This will not save the state when the parent store stops, so that, usually, if the user left on purpose,
     * the state is not persisted.
     * @see [SaveBehavior]
     * @see [saveStatePlugin]
     */
    public data class OnUnsubscribe(val remainingSubscribers: Int = 0) : SaveBehavior

    /**
     * Save the data periodically once [delay] period of time has passed.
     *
     * First save will occur **after** the delay defined.
     *
     * Do not use multiple values of this strategy, because only the smallest value will be respected.
     */
    public data class Periodic(val delay: Duration = 5.seconds) : SaveBehavior

    @Suppress("UndocumentedPublicClass") // document a companion?
    public companion object {

        private const val DefaultDelayMs: Int = 2000

        /**
         * A default [SaveBehavior] that saves the state both on each change with a delay,
         * and when all subscribers leave.
         * @see [saveStatePlugin]
         */
        public val Default: Set<SaveBehavior> = setOf(OnChange(), OnUnsubscribe())
    }
}
