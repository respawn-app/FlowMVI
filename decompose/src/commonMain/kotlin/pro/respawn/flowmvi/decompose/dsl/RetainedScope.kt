// TODO: https://github.com/arkivanov/Essenty/issues/158
@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package pro.respawn.flowmvi.decompose.dsl

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.arkivanov.essenty.instancekeeper.getOrCreate
import com.arkivanov.essenty.lifecycle.coroutines.immediateOrFallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import pro.respawn.flowmvi.decompose.api.RetainedScope
import kotlin.coroutines.CoroutineContext

public fun createRetainedScope(
    context: CoroutineContext = Dispatchers.Main.immediateOrFallback
): RetainedScope = object : RetainedScope, CoroutineScope by CoroutineScope(context + SupervisorJob(context[Job])) {}

public fun InstanceKeeper.retainedScope(
    context: CoroutineContext = Dispatchers.Main.immediateOrFallback,
): CoroutineScope = getOrCreate("CoroutineScope") { createRetainedScope(context) }

public fun ComponentContext.retainedScope(
    context: CoroutineContext = Dispatchers.Main.immediateOrFallback,
): CoroutineScope = instanceKeeper.retainedScope(context)
