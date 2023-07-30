@file:Suppress("StringShouldBeRawString")

package pro.respawn.flowmvi.plugins

import pro.respawn.flowmvi.api.FlowMVIDSL
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.StorePlugin
import pro.respawn.flowmvi.dsl.StoreBuilder
import pro.respawn.flowmvi.plugins.StoreLogLevel.Debug
import pro.respawn.flowmvi.plugins.StoreLogLevel.Error
import pro.respawn.flowmvi.plugins.StoreLogLevel.Info
import pro.respawn.flowmvi.plugins.StoreLogLevel.Trace

public enum class StoreLogLevel {
    Trace, Debug, Info, Warn, Error
}

@FlowMVIDSL
public fun <S : MVIState, I : MVIIntent, A : MVIAction> StoreBuilder<S, I, A>.logging(
    log: (level: StoreLogLevel, tag: String, msg: String) -> Unit
): Unit = install(loggingPlugin(name, log))

@FlowMVIDSL
public inline fun <S : MVIState, I : MVIIntent, A : MVIAction> loggingPlugin(
    tag: String?,
    crossinline log: (level: StoreLogLevel, tag: String, msg: String) -> Unit,
): StorePlugin<S, I, A> = genericPlugin {
    val name = tag ?: "Logging"
    this.name = name
    onState { old, new -> log(Trace, name, "\nState:\n--->\n$old\n<---\n$new") }
    onIntent { log(Debug, name, "Intent -> $it") }
    onAction { log(Debug, name, "Action -> $it") }
    onException { log(Error, name, "Exception:\n ${it.stackTraceToString()}") }
    onStart { log(Info, name, "Started") }
    onSubscribe { log(Info, name, "New subscriber #${it + 1}") }
    onStop { log(Info, name, "Stopped") }
}

public fun <S : MVIState, I : MVIIntent, A : MVIAction> consoleLoggingPlugin(tag: String): StorePlugin<S, I, A> =
    loggingPlugin(tag) { _, _, msg -> println("$tag: $msg") }
