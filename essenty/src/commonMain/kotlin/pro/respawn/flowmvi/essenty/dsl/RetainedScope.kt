package pro.respawn.flowmvi.essenty.dsl

import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.arkivanov.essenty.instancekeeper.InstanceKeeperOwner
import com.arkivanov.essenty.instancekeeper.getOrCreate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import pro.respawn.flowmvi.essenty.internal.RetainedScope
import pro.respawn.flowmvi.util.immediateOrDefault
import kotlin.coroutines.CoroutineContext

private const val DefaultScopeKey = "CoroutineScope"

internal fun createRetainedScope(
    context: CoroutineContext = Dispatchers.Main.immediateOrDefault
): RetainedScope = object : RetainedScope, CoroutineScope by CoroutineScope(context + SupervisorJob(context[Job])) {}

/**
 * Creates OR obtains a [CoroutineScope]
 * instance that is retained across configuration changes using this [InstanceKeeper].
 *
 * Uses an [immediateOrDefault] dispatcher as the coroutine context by default.
 */
public fun InstanceKeeper.retainedScope(
    context: CoroutineContext = Dispatchers.Main.immediateOrDefault,
    key: String = DefaultScopeKey,
): CoroutineScope = getOrCreate(key) { createRetainedScope(context) }

/**
 * Creates OR obtains a [CoroutineScope]
 * instance that is retained across configuration changes using this [InstanceKeeper].
 *
 * Uses an [immediateOrDefault] dispatcher as the coroutine context by default.
 */
public fun InstanceKeeperOwner.retainedScope(
    context: CoroutineContext = Dispatchers.Main.immediateOrDefault,
    key: String = DefaultScopeKey,
): CoroutineScope = instanceKeeper.retainedScope(context, key)
