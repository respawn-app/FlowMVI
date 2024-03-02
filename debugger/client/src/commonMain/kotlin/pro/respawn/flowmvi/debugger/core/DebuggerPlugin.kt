package pro.respawn.flowmvi.debugger.core

import io.ktor.client.HttpClient
import pro.respawn.flowmvi.api.FlowMVIDSL
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.StorePlugin
import pro.respawn.flowmvi.debugger.core.models.OutgoingEvent.StoreAction
import pro.respawn.flowmvi.debugger.core.models.OutgoingEvent.StoreException
import pro.respawn.flowmvi.debugger.core.models.OutgoingEvent.StoreIntent
import pro.respawn.flowmvi.debugger.core.models.OutgoingEvent.StoreStarted
import pro.respawn.flowmvi.debugger.core.models.OutgoingEvent.StoreStateChanged
import pro.respawn.flowmvi.debugger.core.models.OutgoingEvent.StoreSubscribed
import pro.respawn.flowmvi.debugger.core.models.OutgoingEvent.StoreUnsubscribed
import pro.respawn.flowmvi.dsl.StoreBuilder
import pro.respawn.flowmvi.dsl.plugin
import pro.respawn.flowmvi.plugins.TimeTravel
import pro.respawn.flowmvi.plugins.compositePlugin
import pro.respawn.flowmvi.plugins.timeTravelPlugin
import pro.respawn.flowmvi.util.nameByType

public const val DefaultHistorySize: Int = 100

@FlowMVIDSL
public fun <S : MVIState, I : MVIIntent, A : MVIAction> debuggerPlugin(
    storeName: String,
    client: HttpClient,
    timeTravel: TimeTravel<S, I, A>,
): StorePlugin<S, I, A> = plugin {
    this.name = "${storeName}DebuggerPlugin"
    with(DebuggerClient(storeName, client, timeTravel)) {
        onStart {
            launch(this)
            send(StoreStarted(storeName))
        }
        onIntent {
            send(StoreIntent(it))
            it
        }
        onAction {
            send(StoreAction(it))
            it
        }
        onState { old, new ->
            send(StoreStateChanged(old, new))
            new
        }
        onException {
            send(StoreException(it))
            it
        }
        onSubscribe {
            send(StoreSubscribed(it))
        }
        onUnsubscribe {
            send(StoreUnsubscribed(it))
        }
        onStop {
            // nothing to do at this point - the debugger will know that store has disconnected
        }
    }
}

public fun <S : MVIState, I : MVIIntent, A : MVIAction> debuggerPlugin(
    storeName: String,
    client: HttpClient,
    historySize: Int = DefaultHistorySize,
): StorePlugin<S, I, A> {
    val tt = TimeTravel<S, I, A>(maxHistorySize = historySize)
    return compositePlugin(
        setOf(
            timeTravelPlugin(tt, "${storeName}DebuggerTimeTravel"),
            debuggerPlugin(storeName, client, tt)
        ),
    )
}

@FlowMVIDSL
public inline fun <reified S : MVIState, I : MVIIntent, A : MVIAction> StoreBuilder<S, I, A>.remoteDebugger(
    client: HttpClient,
    historySize: Int = DefaultHistorySize,
) {
    if (!debuggable) return
    install(debuggerPlugin(name ?: nameByType<S>() ?: "Store", client, historySize))
}
