package pro.respawn.flowmvi.test.plugin

import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.PipelineContext
import pro.respawn.flowmvi.api.StorePlugin
import pro.respawn.flowmvi.plugins.TimeTravel
import pro.respawn.flowmvi.plugins.compositePlugin
import pro.respawn.flowmvi.plugins.timeTravelPlugin
import kotlin.coroutines.CoroutineContext
import kotlin.test.assertFalse
import kotlin.test.assertTrue

public class PluginTestScope<S : MVIState, I : MVIIntent, A : MVIAction> private constructor(
    public val ctx: TestPipelineContext<S, I, A>,
    public val timeTravel: TimeTravel<S, I, A>,
) : PipelineContext<S, I, A> by ctx, StorePlugin<S, I, A> by ctx.plugin {

    public constructor(
        initial: S,
        coroutineContext: CoroutineContext,
        plugin: StorePlugin<S, I, A>,
        timeTravel: TimeTravel<S, I, A>,
    ) : this(
        timeTravel = timeTravel,
        ctx = TestPipelineContext(
            initial = initial,
            coroutineContext = coroutineContext,
            plugin = compositePlugin(setOf(timeTravelPlugin(timeTravel), plugin), plugin.name),
        ),
    )

    public fun shouldBeActive(): Unit = assertFalse { ctx.closed }
    public fun shouldBeClosed(): Unit = assertTrue { ctx.closed }
    public val state: S by ctx::state
}
