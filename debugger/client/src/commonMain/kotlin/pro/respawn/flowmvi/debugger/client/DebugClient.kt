package pro.respawn.flowmvi.debugger.client

import com.benasher44.uuid.uuid4
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.receiveDeserialized
import io.ktor.client.plugins.websocket.sendSerialized
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.client.request.setBody
import io.ktor.http.HttpMethod
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.PipelineContext
import pro.respawn.flowmvi.debugger.DebuggerDefaults
import pro.respawn.flowmvi.debugger.model.ClientEvent
import pro.respawn.flowmvi.debugger.model.ServerEvent
import pro.respawn.flowmvi.debugger.model.StoreConnectionDescriptor
import pro.respawn.flowmvi.debugger.model.StoreEvent
import pro.respawn.flowmvi.logging.PlatformStoreLogger
import pro.respawn.flowmvi.logging.StoreLogLevel.Debug
import pro.respawn.flowmvi.logging.invoke
import pro.respawn.flowmvi.plugins.TimeTravel
import kotlin.time.Duration

internal class DebugClient<S : MVIState, I : MVIIntent, A : MVIAction>(
    private val name: String,
    private val client: HttpClient,
    private val timeTravel: TimeTravel<S, I, A>,
    private val host: String = DebuggerDefaults.ClientHost,
    private val port: Int = DebuggerDefaults.Port,
    private val reconnectionDelay: Duration = DebuggerDefaults.ReconnectionDelay,
) {
    private val id = uuid4()
    private val logger = PlatformStoreLogger

    private val session = MutableStateFlow<DefaultClientWebSocketSession?>(null)
    private var pipeline = atomic<PipelineContext<S, I, A>?>(null)

    fun launch(context: PipelineContext<S, I, A>) {
        require(pipeline.getAndSet(context) == null) { "Debugger is already started" }
        context.launchConnectionLoop {
            client.webSocketSession(
                method = HttpMethod.Get,
                host = host,
                port = port,
                path = "/$id",
            ) {
                setBody(StoreConnectionDescriptor(id, name))
            }
        }
    }

    private fun CoroutineScope.launchConnectionLoop(
        connect: suspend () -> DefaultClientWebSocketSession,
    ) = launch {
        while (true) {
            runCatching {
                logger(Debug) { "Starting connection at $host:$port" }
                val current = connect()
                session.value = current
                current.runReceiveLoop() // will suspend until failure or closed
            }.onFailure {
                logger(Debug) { "Connection failed:" }
                logger(Debug, e = it as Exception)
            }
            logger(Debug) { "Finished session" }
            session.value = null
            delay(reconnectionDelay)
        }
    }

    private suspend fun DefaultClientWebSocketSession.runReceiveLoop() {
        while (true) {
            val event = receiveDeserialized<ServerEvent>().takeIf { it.storeId == id } ?: continue
            when (event) {
                is ServerEvent.Stop -> pipeline.value?.close()
                is ServerEvent.RestoreState -> pipeline.value?.updateState {
                    timeTravel.states.elementAtOrNull(event.index) ?: this
                }
            }
        }
    }

    fun PipelineContext<S, I, A>.send(event: ClientEvent) = launch {
        kotlin.runCatching {
            withTimeoutOrNull(DebuggerDefaults.ReconnectionDelay) {
                session
                    .filterNotNull()
                    .first()
                    .sendSerialized(StoreEvent(event, id))
                    .also { logger(Debug) { "sent event $event to ${session.value?.call?.request?.url}" } }
            }
        }
    }
}
