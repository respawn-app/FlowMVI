package pro.respawn.flowmvi.debugger.server

import io.ktor.server.engine.EmbeddedServer
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.server.util.getOrFail
import io.ktor.server.websocket.receiveDeserialized
import io.ktor.server.websocket.sendSerialized
import io.ktor.server.websocket.webSocket
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.withContext
import pro.respawn.flowmvi.api.Container
import pro.respawn.flowmvi.debugger.model.ClientEvent
import pro.respawn.flowmvi.debugger.model.ClientEvent.StoreDisconnected
import pro.respawn.flowmvi.debugger.model.ServerEvent
import pro.respawn.flowmvi.debugger.server.ServerAction.SendClientEvent
import pro.respawn.flowmvi.debugger.server.ServerIntent.EventReceived
import pro.respawn.flowmvi.debugger.server.ServerIntent.ServerStarted
import pro.respawn.flowmvi.debugger.server.ServerIntent.StopRequested
import pro.respawn.flowmvi.logging.PlatformStoreLogger
import pro.respawn.flowmvi.logging.StoreLogLevel
import pro.respawn.flowmvi.logging.invoke
import pro.respawn.kmmutils.common.asUUID

internal object DebugServer : Container<ServerState, ServerIntent, ServerAction> {

    override val store by debugServerStore()
    private var server: EmbeddedServer<*, *>? by atomic(null)
    private val logger = PlatformStoreLogger

    fun start(host: String, port: Int) = embeddedServer(Netty, port = port, host = host) {
        configureDebugServer()
        // store will be started / closed along with the server
        store.start(this)
        store.intent(ServerStarted)
        routing {
            get("/") { call.respondText("FlowMVI Debugger Online", null) }
            webSocket("/{id}") {
                val storeId = call.parameters.getOrFail("id").asUUID
                with(store) {
                    try {
                        subscribe {
                            actions
                                .filterIsInstance<SendClientEvent>()
                                .filter { it.client == storeId }
                                .collect { sendSerialized<ServerEvent>(it.event) }
                        }
                        while (true) {
                            val event = receiveDeserialized<ClientEvent>()
                            intent(EventReceived(event, storeId))
                        }
                    } finally {
                        logger(StoreLogLevel.Debug) { "Store $storeId disconnected" }
                        intent(EventReceived(StoreDisconnected(storeId), storeId))
                    }
                }
            }
        }
    }
        .also { server = it }
        .start()

    suspend fun stop() = withContext(Dispatchers.IO) {
        store.intent(StopRequested)
        server?.stop()
        server = null
    }
}
