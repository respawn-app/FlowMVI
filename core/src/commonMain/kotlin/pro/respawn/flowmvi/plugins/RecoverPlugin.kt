package pro.respawn.flowmvi.plugins

import pro.respawn.flowmvi.api.FlowMVIDSL
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.PipelineContext
import pro.respawn.flowmvi.api.StorePlugin
import pro.respawn.flowmvi.dsl.StoreBuilder
import pro.respawn.flowmvi.dsl.plugin

public typealias Recover<S, I, A> = suspend PipelineContext<S, I, A>.(e: Exception) -> Exception?

/**
 * Create and install a [recoverPlugin].
 */
@FlowMVIDSL
public inline fun <S : MVIState, I : MVIIntent, A : MVIAction> StoreBuilder<S, I, A>.recover(
    name: String? = null,
    @BuilderInference noinline recover: Recover<S, I, A>,
): Unit = install(recoverPlugin(name, recover))

/**
 * Create a plugin that simply invokes [StorePlugin.onException] and decides how to proceed accordingly.
 * See the parent function for more information.
 */
@FlowMVIDSL
public inline fun <S : MVIState, I : MVIIntent, A : MVIAction> recoverPlugin(
    name: String? = null,
    @BuilderInference noinline recover: Recover<S, I, A>
): StorePlugin<S, I, A> = plugin {
    this.name = name
    onException(recover)
}
