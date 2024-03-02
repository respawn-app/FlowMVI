package pro.respawn.flowmvi.debugger.core

import com.benasher44.uuid.uuid4
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.receiveDeserialized
import io.ktor.client.plugins.websocket.sendSerialized
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.client.request.setBody
import io.ktor.http.HttpMethod
import io.ktor.websocket.close
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.PipelineContext
import pro.respawn.flowmvi.debugger.core.models.IncomingEvent
import pro.respawn.flowmvi.debugger.core.models.OutgoingEvent
import pro.respawn.flowmvi.debugger.core.models.StoreConnectionDescriptor
import pro.respawn.flowmvi.debugger.core.models.StoreEvent
import pro.respawn.flowmvi.plugins.TimeTravel
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

internal class DebuggerClient<S : MVIState, I : MVIIntent, A : MVIAction>(
    private val name: String,
    private val client: HttpClient,
    private val timeTravel: TimeTravel<S, I, A>,
    private val host: String = "127.0.0.1",
    private val port: Int = 6780,
    private val reconnectionDelay: Duration = 20.seconds,
) {

    private val id = uuid4()

    private val session = MutableStateFlow<DefaultClientWebSocketSession?>(null)
    private var pipeline = atomic<PipelineContext<S, I, A>?>(null)

    fun launch(context: PipelineContext<S, I, A>) {
        require(pipeline.getAndSet(context) == null) { "Debugger is already started" }
        context.launchConnectionLoop {
            client.webSocketSession(
                method = HttpMethod.Get,
                host = host,
                port = port,
                path = "/flowmvi",
            ) {
                setBody(StoreConnectionDescriptor(id, name))
            }
        }.invokeOnCompletion { pipeline.getAndSet(null) }
    }

    private fun CoroutineScope.launchConnectionLoop(
        connect: suspend () -> DefaultClientWebSocketSession,
    ) = launch {
        while (true) {
            runCatching {
                val current = connect()
                session.value = current
                current.runReceiveLoop() // will suspend until failure or closed
            }
            session.value = null
            delay(reconnectionDelay)
        }
    }

    private suspend fun DefaultClientWebSocketSession.runReceiveLoop() {
        while (isActive) {
            val event = receiveDeserialized<IncomingEvent>().takeIf { it.storeId == id } ?: continue
            when (event) {
                is IncomingEvent.Stop -> pipeline.value?.close()
                is IncomingEvent.RestoreState -> pipeline.value?.updateState {
                    timeTravel.states.elementAtOrNull(event.index) ?: this
                }
            }
        }
    }

    fun PipelineContext<S, I, A>.send(event: OutgoingEvent) = launch {
        withTimeoutOrNull(reconnectionDelay) {
            session
                .filterNotNull()
                .filter { it.isActive }
                .first()
                .sendSerialized(StoreEvent(event, id))
        }
    }
}
