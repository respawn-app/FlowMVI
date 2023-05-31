package pro.respawn.flowmvi.plugin

import pro.respawn.flowmvi.MVIIntent
import pro.respawn.flowmvi.MVIState
import pro.respawn.flowmvi.base.PipelineContext
import pro.respawn.flowmvi.base.StorePlugin

private val RecoverPluginName = "RecoverPlugin"

public fun <S : MVIState, I : MVIIntent> recoverPlugin(
    recover: PipelineContext<S, I>.(e: Exception) -> Boolean
): StorePlugin<S, I> =
    object : StorePlugin<S, I> {
        override fun PipelineContext<S, I>.onException(e: Exception) = recover(e)
        override val name = RecoverPluginName
    }
