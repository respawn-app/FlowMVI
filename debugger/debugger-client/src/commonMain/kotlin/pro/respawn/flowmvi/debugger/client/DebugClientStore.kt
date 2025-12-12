package pro.respawn.flowmvi.debugger.client

import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.receiveDeserialized
import io.ktor.client.plugins.websocket.sendSerialized
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.http.HttpMethod
import io.ktor.websocket.close
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.SerializationException
import pro.respawn.flowmvi.api.ActionShareBehavior
import pro.respawn.flowmvi.api.EmptyState
import pro.respawn.flowmvi.api.Store
import pro.respawn.flowmvi.debugger.model.ClientEvent
import pro.respawn.flowmvi.debugger.model.ClientEvent.StoreConnected
import pro.respawn.flowmvi.debugger.model.ServerEvent
import pro.respawn.flowmvi.dsl.store
import pro.respawn.flowmvi.logging.StoreLogLevel
import pro.respawn.flowmvi.logging.StoreLogger
import pro.respawn.flowmvi.logging.invoke
import pro.respawn.flowmvi.logging.log
import pro.respawn.flowmvi.plugins.enableLogging
import pro.respawn.flowmvi.plugins.init
import pro.respawn.flowmvi.plugins.recover
import pro.respawn.flowmvi.plugins.reduce
import kotlin.time.Duration
import kotlin.uuid.Uuid

internal typealias DebugClientStore = Store<EmptyState, ClientEvent, ServerEvent>

internal fun debugClientStore(
    clientName: String,
    client: HttpClient,
    host: String,
    port: Int,
    reconnectionDelay: Duration,
    logEvents: Boolean = false,
) = store(EmptyState) {
    val id = Uuid.random()
    val session = MutableStateFlow<DefaultClientWebSocketSession?>(null)
    configure {
        name = "${clientName}Debugger"
        coroutineContext = Dispatchers.Default
        debuggable = true
        parallelIntents = false // ensure the order of events matches server's expectations
        actionShareBehavior = ActionShareBehavior.Distribute()
        allowIdleSubscriptions = true
        onOverflow = BufferOverflow.DROP_OLDEST // drop old events in the queue
    }
    if (logEvents) enableLogging()
    recover {
        log(it)
        null
    }
    init {
        launchConnectionLoop(
            reconnectionDelay = reconnectionDelay,
            onError = {
                session.update {
                    it?.close()
                    null
                }
            },
        ) {
            log(StoreLogLevel.Trace) { "Starting connection at $host:$port/$id" }
            val _ = client.webSocketSession(
                method = HttpMethod.Get,
                host = host,
                port = port,
                path = "/$id",
            ).apply {
                session.update {
                    it?.close()
                    this
                }
                sendSerialized<ClientEvent>(StoreConnected(clientName, id))
                log(StoreLogLevel.Trace) { "Established connection to ${call.request.url}" }
                awaitEvents(config.logger) {
                    if (it.storeId == id) action(it)
                }
            }
        }
    }

    reduce { intent ->
        withTimeoutOrNull(reconnectionDelay) {
            session.filterNotNull().first().apply {
                sendSerialized<ClientEvent>(intent)
            }
        }
    }
}

private inline fun CoroutineScope.launchConnectionLoop(
    reconnectionDelay: Duration,
    crossinline onError: suspend (Exception) -> Unit,
    crossinline connect: suspend () -> Unit,
) = launch {
    while (isActive) {
        try {
            supervisorScope {
                connect()
                awaitCancellation()
            }
        } catch (e: CancellationException) {
            onError(e)
            throw e
        } catch (expected: Exception) {
            onError(expected)
        }
        delay(reconnectionDelay)
    }
}

private suspend inline fun DefaultClientWebSocketSession.awaitEvents(
    log: StoreLogger,
    onEvent: (ServerEvent) -> Unit
) {
    while (isActive) {
        try {
            val event = receiveDeserialized<ServerEvent>()
            log(StoreLogLevel.Trace) { "Received event: $event" }
            onEvent(event)
        } catch (e: Exception) {
            log(e, StoreLogLevel.Warn)
            continue
        }
    }
}
