package pro.respawn.flowmvi.debugger.server.arch.configuration

import kotlinx.serialization.KSerializer
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.dsl.StoreBuilder
import pro.respawn.flowmvi.savedstate.api.Saver
import pro.respawn.flowmvi.savedstate.dsl.TypedSaver

inline fun <reified S : MVIState, I : MVIIntent, A : MVIAction> StoreBuilder<S, I, A>.configure(
    configuration: StoreConfiguration,
    name: String,
    saver: Saver<S>? = null,
) = with(configuration) {
    invoke(
        name = name,
        saver = saver,
    )
}

inline fun <reified T : S, reified S : MVIState, I : MVIIntent, A : MVIAction> StoreBuilder<S, I, A>.configure(
    configuration: StoreConfiguration,
    serializer: KSerializer<T>,
    name: String,
) = with(configuration) {
    invoke(
        name = name,
        saver = TypedSaver<T, S>(saver(serializer, "${name}State")),
    )
}
