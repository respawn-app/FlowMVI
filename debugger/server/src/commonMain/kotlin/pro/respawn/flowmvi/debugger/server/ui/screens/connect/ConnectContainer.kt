package pro.respawn.flowmvi.debugger.server.ui.screens.connect

import kotlinx.coroutines.launch
import pro.respawn.apiresult.runResulting
import pro.respawn.flowmvi.api.Container
import pro.respawn.flowmvi.api.PipelineContext
import pro.respawn.flowmvi.debugger.server.DebugServer
import pro.respawn.flowmvi.debugger.server.arch.configuration.StoreConfiguration
import pro.respawn.flowmvi.debugger.server.arch.configuration.configure
import pro.respawn.flowmvi.debugger.server.ui.screens.connect.ConnectAction.GoToTimeline
import pro.respawn.flowmvi.debugger.server.ui.screens.connect.ConnectIntent.HostChanged
import pro.respawn.flowmvi.debugger.server.ui.screens.connect.ConnectIntent.PortChanged
import pro.respawn.flowmvi.debugger.server.ui.screens.connect.ConnectIntent.RetryClicked
import pro.respawn.flowmvi.debugger.server.ui.screens.connect.ConnectIntent.StartServerClicked
import pro.respawn.flowmvi.debugger.server.ui.screens.connect.ConnectState.ConfiguringServer
import pro.respawn.flowmvi.debugger.server.ui.util.HostForm
import pro.respawn.flowmvi.debugger.server.ui.util.PortForm
import pro.respawn.flowmvi.dsl.store
import pro.respawn.flowmvi.dsl.updateState
import pro.respawn.flowmvi.dsl.updateStateImmediate
import pro.respawn.flowmvi.plugins.recover
import pro.respawn.flowmvi.plugins.reduce

private typealias Ctx = PipelineContext<ConnectState, ConnectIntent, ConnectAction>

internal class ConnectContainer(
    configuration: StoreConfiguration,
) : Container<ConnectState, ConnectIntent, ConnectAction> {

    override val store = store(ConfiguringServer()) {
        configure(configuration, ConfiguringServer.serializer(), "ConnectStore")
        recover {
            updateState { ConnectState.Error(it) }
            null
        }
        reduce { intent ->
            when (intent) {
                is HostChanged -> updateStateImmediate<ConfiguringServer, _> { copy(host = HostForm(intent.host)) }
                is PortChanged -> updateStateImmediate<ConfiguringServer, _> { copy(port = PortForm(intent.port)) }
                is RetryClicked -> updateState {
                    runResulting { DebugServer.stop() } // try to stop the server if possible
                    ConfiguringServer()
                }
                is StartServerClicked -> updateState<ConfiguringServer, _> {
                    if (!canStart) return@updateState this
                    val previous = this
                    launch {
                        DebugServer.start(host = host.value, port = port.value.toInt())
                        action(GoToTimeline)
                        updateState { previous }
                    }
                    ConnectState.Loading
                }
            }
        }
    }
}
