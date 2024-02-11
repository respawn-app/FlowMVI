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

/**
 * A class which provides DSL for testing a [StorePlugin].
 *
 * Contains:
 * * [ctx] or `this` - a mock pipeline context configured specifically for testing.
 * * [TestPipelineContext.plugin] - the plugin being tested
 * * [timeTravel] embedded time travel plugin to track any side effects that the plugin produces.
 *
 * See [StorePlugin.test] for a function that allows to test the plugin
 */
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
