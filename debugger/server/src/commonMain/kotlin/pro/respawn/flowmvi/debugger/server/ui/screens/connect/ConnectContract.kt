@file:UseSerializers(InputSerializer::class)
package pro.respawn.flowmvi.debugger.server.ui.screens.connect

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.debugger.DebuggerDefaults
import pro.respawn.flowmvi.debugger.server.util.InputSerializer
import pro.respawn.kmmutils.inputforms.Input
import pro.respawn.kmmutils.inputforms.dsl.input
import pro.respawn.kmmutils.inputforms.dsl.isValid

@Immutable
internal sealed interface ConnectState : MVIState {
    data object Loading : ConnectState
    data class Error(val e: Exception) : ConnectState

    @Serializable
    data class ConfiguringServer(
        val host: Input = input(DebuggerDefaults.LocalHost),
        val port: Input = input(DebuggerDefaults.Port.toString()),
    ) : ConnectState {

        val canStart = host.isValid && port.isValid
    }
}

@Immutable
internal sealed interface ConnectIntent : MVIIntent {

    data class PortChanged(val port: String) : ConnectIntent
    data class HostChanged(val host: String) : ConnectIntent
    data object StartServerClicked : ConnectIntent
    data object RetryClicked : ConnectIntent
}

@Immutable
internal sealed interface ConnectAction : MVIAction {
    data object GoToTimeline : ConnectAction
}
