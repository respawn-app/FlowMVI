package pro.respawn.flowmvi.test.plugin

import kotlinx.coroutines.coroutineScope
import pro.respawn.flowmvi.api.FlowMVIDSL
import pro.respawn.flowmvi.api.LazyPlugin
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.StorePlugin
import pro.respawn.flowmvi.dsl.StoreConfigurationBuilder
import pro.respawn.flowmvi.dsl.configuration
import pro.respawn.flowmvi.plugins.TimeTravel

/**
 * A function that runs a test on a [StorePlugin].
 *
 * * This function suspends until the test is complete.
 * * The plugin may launch new coroutines, which will cause the test to suspend until the test scope is exited.
 * * The plugin may produce side effects which are tracked in the [PluginTestScope.timeTravel] property.
 * * The plugin may change the state, which is accessible via the [PluginTestScope.state] property.
 * * You can use [TestPipelineContext] which is provided with the [PluginTestScope], to set up the plugin's
 * environment for the test.
 */
@FlowMVIDSL
public suspend fun <S : MVIState, I : MVIIntent, A : MVIAction> LazyPlugin<S, I, A>.test(
    initial: S,
    timeTravel: TimeTravel<S, I, A> = TimeTravel(),
    configuration: StoreConfigurationBuilder.() -> Unit = { debuggable = true },
    block: suspend PluginTestScope<S, I, A>.() -> Unit,
): Unit = coroutineScope {
    val config = configuration(initial, configuration)
    val scope = PluginTestScope(config, this@test, timeTravel, this)
    try {
        scope.block()
    } finally {
        scope.closeAndWait()
    }
}
