package pro.respawn.flowmvi.plugins

import pro.respawn.flowmvi.api.FlowMVIDSL
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.PipelineContext
import pro.respawn.flowmvi.api.StorePlugin
import pro.respawn.flowmvi.dsl.StoreBuilder
import pro.respawn.flowmvi.dsl.storePlugin

public typealias Recover<S, I, A> = suspend PipelineContext<S, I, A>.(e: Exception) -> Exception?

@FlowMVIDSL
public fun <S : MVIState, I : MVIIntent, A : MVIAction> StoreBuilder<S, I, A>.recover(
    name: String? = null,
    recover: Recover<S, I, A>,
): StorePlugin<S, I, A> = install(recoverPlugin(name, recover))

@FlowMVIDSL
public fun <S : MVIState, I : MVIIntent, A : MVIAction> recoverPlugin(
    name: String? = null,
    recover: Recover<S, I, A>
): StorePlugin<S, I, A> = storePlugin {
    this.name = name
    onException(recover)
}
