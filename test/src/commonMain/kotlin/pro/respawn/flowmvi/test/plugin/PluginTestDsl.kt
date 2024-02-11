package pro.respawn.flowmvi.test.plugin

import pro.respawn.flowmvi.api.FlowMVIDSL
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.StorePlugin
import pro.respawn.flowmvi.plugins.TimeTravel
import kotlin.coroutines.coroutineContext

@FlowMVIDSL
public suspend inline fun <S : MVIState, I : MVIIntent, A : MVIAction> StorePlugin<S, I, A>.test(
    initial: S,
    timeTravel: TimeTravel<S, I, A> = TimeTravel(),
    block: PluginTestScope<S, I, A>.() -> Unit,
): Unit = PluginTestScope(initial, coroutineContext, this, timeTravel).block()
