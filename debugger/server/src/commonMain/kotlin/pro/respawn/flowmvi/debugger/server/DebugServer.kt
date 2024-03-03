package pro.respawn.flowmvi.debugger.server

import io.ktor.http.HttpStatusCode
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.request.receive
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.server.util.getOrFail
import io.ktor.server.websocket.receiveDeserialized
import io.ktor.server.websocket.sendSerialized
import io.ktor.server.websocket.webSocket
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.supervisorScope
import pro.respawn.flowmvi.api.Container
import pro.respawn.flowmvi.debugger.DebuggerDefaults
import pro.respawn.flowmvi.debugger.model.ClientEvent
import pro.respawn.flowmvi.debugger.model.StoreConnectionDescriptor
import pro.respawn.flowmvi.debugger.server.ServerAction.SendClientEvent
import pro.respawn.flowmvi.debugger.server.ServerIntent.EventReceived
import pro.respawn.flowmvi.debugger.server.ServerIntent.StoreConnected
import pro.respawn.flowmvi.debugger.server.ServerIntent.StoreDisconnected
import pro.respawn.kmmutils.common.asUUID

internal object DebugServer : Container<ServerState, ServerIntent, ServerAction> {

    override val store by debugServerStore()

    private val server = embeddedServer(Netty, port = DebuggerDefaults.Port, host = DebuggerDefaults.Host) {
        configureDebugServer()
        // store will be started / closed along with the server
        store.start(this)
        routing {
            get("/") { call.respond(HttpStatusCode.OK, null) }
            webSocket("/{id}") {
                val storeId = call.parameters.getOrFail("id").asUUID
                with(store) {
                    intent(StoreConnected(call.receive<StoreConnectionDescriptor>()))
                    try {
                        supervisorScope {
                            subscribe {
                                actions
                                    .filterIsInstance<SendClientEvent>()
                                    .filter { it.client == storeId }
                                    .collect { sendSerialized(it.event) }
                            }
                            while (true) {
                                val event = receiveDeserialized<ClientEvent>()
                                intent(EventReceived(event, storeId))
                            }
                        }
                    } finally {
                        intent(StoreDisconnected(storeId))
                    }
                }
            }
        }
    }.start()

    fun relaunch() = server.reload()
}
