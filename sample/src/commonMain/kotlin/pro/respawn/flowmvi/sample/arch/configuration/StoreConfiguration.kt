package pro.respawn.flowmvi.sample.arch.configuration

import kotlinx.serialization.KSerializer
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.dsl.StoreBuilder
import pro.respawn.flowmvi.savedstate.api.Saver

interface StoreConfiguration {

    operator fun <S : MVIState, I : MVIIntent, A : MVIAction> StoreBuilder<S, I, A>.invoke(
        name: String,
        saver: Saver<S>? = null,
    )

    fun <S : MVIState> saver(serializer: KSerializer<S>, fileName: String): Saver<S>
}
