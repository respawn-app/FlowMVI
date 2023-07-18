@file:Suppress("StringShouldBeRawString")

package pro.respawn.flowmvi.plugins

import kotlinx.atomicfu.atomic
import pro.respawn.flowmvi.api.FlowMVIDSL
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.StorePlugin
import pro.respawn.flowmvi.dsl.StoreBuilder
import pro.respawn.flowmvi.plugins.LogPriority.Debug
import pro.respawn.flowmvi.plugins.LogPriority.Error
import pro.respawn.flowmvi.plugins.LogPriority.Info
import pro.respawn.flowmvi.plugins.LogPriority.Trace

public enum class LogPriority {
    Trace, Debug, Info, Warn, Error
}

@FlowMVIDSL
public fun <S : MVIState, I : MVIIntent, A : MVIAction> StoreBuilder<S, I, A>.logging(
    log: (priority: LogPriority, tag: String, msg: String) -> Unit
): Unit = install(loggingPlugin(name, log))

@FlowMVIDSL
public inline fun <S : MVIState, I : MVIIntent, A : MVIAction> loggingPlugin(
    tag: String,
    crossinline log: (priority: LogPriority, tag: String, msg: String) -> Unit,
): StorePlugin<S, I, A> = genericPlugin(tag) {
    var subscriptionCount by atomic(0)

    onState { old, new -> log(Trace, tag, "\nState:\n--->\n$old\n<---\n$new") }

    onIntent { log(Debug, tag, "Intent -> $it") }

    onAction { log(Debug, tag, "Action -> $it") }

    onException { log(Error, tag, "Exception:\n ${it.stackTraceToString()}") }

    onStart { log(Info, tag, "started") }

    onSubscribe { log(Info, tag, "New subscriber #${++subscriptionCount}") }

    onStop { log(Info, tag, "stopped") }
}

public fun <S : MVIState, I : MVIIntent, A : MVIAction> consoleLoggingPlugin(name: String): StorePlugin<S, I, A> =
    loggingPlugin(name) { _, tag, msg -> println("$tag: $msg") }
