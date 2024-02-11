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

public class PluginTestScope<S : MVIState, I : MVIIntent, A : MVIAction> private constructor(
    private val ctx: TestPipelineContext<S, I, A>,
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

    // compiler bug which crashes compilation because both context and plugin declare equals
    override fun equals(other: Any?): Boolean = ctx.plugin == other
    override fun hashCode(): Int = ctx.plugin.hashCode()
    public val state: S by ctx::state
}
