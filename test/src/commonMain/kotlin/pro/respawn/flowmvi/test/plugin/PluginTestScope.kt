package pro.respawn.flowmvi.test.plugin

import pro.respawn.flowmvi.annotation.NotIntendedForInheritance
import pro.respawn.flowmvi.api.LazyPlugin
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.PipelineContext
import pro.respawn.flowmvi.api.StoreConfiguration
import pro.respawn.flowmvi.api.StorePlugin
import pro.respawn.flowmvi.api.context.ShutdownContext
import pro.respawn.flowmvi.plugins.TimeTravel
import pro.respawn.flowmvi.plugins.loggingPlugin
import pro.respawn.flowmvi.plugins.timeTravelPlugin

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
@OptIn(NotIntendedForInheritance::class)
public class PluginTestScope<S : MVIState, I : MVIIntent, A : MVIAction> private constructor(
    private val ctx: TestPipelineContext<S, I, A>,
    public val timeTravel: TimeTravel<S, I, A>,
) : PipelineContext<S, I, A> by ctx,
    ShutdownContext<S, I, A>,
    StorePlugin<S, I, A> by ctx.plugin {

    @PublishedApi
    internal constructor(
        configuration: StoreConfiguration<S>,
        plugin: LazyPlugin<S, I, A>,
        timeTravel: TimeTravel<S, I, A>,
    ) : this(
        timeTravel = timeTravel,
        ctx = with(configuration) {
            val plugin = plugin(this)
            val log = loggingPlugin<S, I, A>()(this)
            val tt = timeTravelPlugin(timeTravel)
            TestPipelineContext(
                config = configuration,
                name = plugin.name,
                plugins = listOf(plugin, log, tt)
            )
        }
    )

    // compiler bug which crashes compilation because both context and plugin declare equals
    override fun equals(other: Any?): Boolean = ctx.plugin == other
    override fun hashCode(): Int = ctx.plugin.hashCode()
    override fun toString(): String = "PluginTestScope(plugin=${ctx.plugin.name})"
}
