package pro.respawn.flowmvi.savedstate.api

import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

public sealed interface SaveBehavior {

    public data class OnChange(val timeout: Duration = DefaultSaveTimeoutMs.milliseconds) : SaveBehavior

    public data class OnUnsubscribe(val remainingSubscribers: Int = 0) : SaveBehavior

    public companion object {

        public val Default: Set<SaveBehavior> = setOf(OnChange(), OnUnsubscribe())
        public const val DefaultSaveTimeoutMs: Int = 2000
    }
}
