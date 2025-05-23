package pro.respawn.flowmvi.debugger.server.arch.configuration

import kotlinx.coroutines.channels.BufferOverflow.SUSPEND
import kotlinx.serialization.KSerializer
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.StateStrategy
import pro.respawn.flowmvi.dsl.StoreBuilder
import pro.respawn.flowmvi.plugins.enableLogging
import pro.respawn.flowmvi.savedstate.api.Saver
import pro.respawn.flowmvi.savedstate.dsl.NoOpSaver

object TestStoreConfiguration : StoreConfiguration {

    override fun <S : MVIState, I : MVIIntent, A : MVIAction> StoreBuilder<S, I, A>.invoke(
        name: String,
        saver: Saver<S>?,
    ) {
        configure {
            this.name = name
            debuggable = true
            parallelIntents = false
            stateStrategy = StateStrategy.Immediate
            onOverflow = SUSPEND
        }
        enableLogging()
    }

    override fun <S : MVIState> saver(serializer: KSerializer<S>, fileName: String) = NoOpSaver<S>()
}
