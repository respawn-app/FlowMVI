package pro.respawn.flowmvi.sample.arch.configuration

import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.debugger.plugin.debuggerPlugin
import pro.respawn.flowmvi.dsl.StoreBuilder

actual fun <S : MVIState, I : MVIIntent, A : MVIAction> StoreBuilder<S, I, A>.remoteDebugger() {
    install(debuggerPlugin(requireNotNull(name)))
}
