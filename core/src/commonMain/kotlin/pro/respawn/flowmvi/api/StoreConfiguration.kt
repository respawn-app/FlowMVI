package pro.respawn.flowmvi.api

import kotlinx.coroutines.channels.BufferOverflow
import pro.respawn.flowmvi.dsl.StoreConfigurationBuilder
import pro.respawn.flowmvi.logging.StoreLogger
import kotlin.coroutines.CoroutineContext

/**
 * A configuration of the [Store].
 *
 * Please see [StoreConfigurationBuilder] for details on the meaning behind the properties listed here
 */
@ConsistentCopyVisibility
@Suppress("UndocumentedPublicProperty")
public data class StoreConfiguration<S : MVIState> internal constructor(
    val initial: S,
    val allowIdleSubscriptions: Boolean,
    val parallelIntents: Boolean,
    val actionShareBehavior: ActionShareBehavior,
    val stateStrategy: StateStrategy,
    val intentCapacity: Int,
    val onOverflow: BufferOverflow,
    val debuggable: Boolean,
    val coroutineContext: CoroutineContext,
    val logger: StoreLogger,
    val verifyPlugins: Boolean,
    val name: String?,
) {

    @Deprecated(
        "Please use the StateStrategy directly",
        ReplaceWith("this.stateStrategy is StateStrategy.Atomic")
    )
    val atomicStateUpdates: Boolean get() = stateStrategy is StateStrategy.Atomic
}
