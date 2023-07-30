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
): StorePlugin<S, I, A> = install(loggingPlugin(name, log))

@FlowMVIDSL
public inline fun <S : MVIState, I : MVIIntent, A : MVIAction> loggingPlugin(
    tag: String,
    crossinline log: (level: StoreLogLevel, tag: String, msg: String) -> Unit,
): StorePlugin<S, I, A> = genericPlugin {
    name = tag
    onState { old, new -> log(Trace, tag, "\nState:\n--->\n$old\n<---\n$new") }
    onIntent { log(Debug, tag, "Intent -> $it") }
    onAction { log(Debug, tag, "Action -> $it") }
    onException { log(Error, tag, "Exception:\n ${it.stackTraceToString()}") }
    onStart { log(Info, tag, "Started") }
    onSubscribe { log(Info, tag, "New subscriber #${it + 1}") }
    onStop { log(Info, tag, "Stopped") }
}

public fun <S : MVIState, I : MVIIntent, A : MVIAction> consoleLoggingPlugin(tag: String): StorePlugin<S, I, A> =
    loggingPlugin(tag) { _, _, msg -> println("$tag: $msg") }
