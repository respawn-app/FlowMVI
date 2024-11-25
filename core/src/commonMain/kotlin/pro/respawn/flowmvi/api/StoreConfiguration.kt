package pro.respawn.flowmvi.api

import kotlinx.coroutines.channels.BufferOverflow
import pro.respawn.flowmvi.dsl.StoreConfigurationBuilder
import pro.respawn.flowmvi.logging.StoreLogger
import kotlin.coroutines.CoroutineContext

/**
 * A configuration of the [Store].
 * Please see [StoreConfigurationBuilder] for details on the meaning behind the properties listed here
 *
 * @param initial The initial state the [Store] will have.
 */
@Suppress("UndocumentedPublicProperty")
public data class StoreConfiguration<S : MVIState>(
    val initial: S,
    val allowIdleSubscriptions: Boolean,
    val parallelIntents: Boolean,
    val actionShareBehavior: ActionShareBehavior,
    val intentCapacity: Int,
    val onOverflow: BufferOverflow,
    val debuggable: Boolean,
    val coroutineContext: CoroutineContext,
    val logger: StoreLogger,
    val atomicStateUpdates: Boolean,
    val verifyPlugins: Boolean,
    val name: String?,
)
