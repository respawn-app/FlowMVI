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

/**
 * Default tag for the [loggingPlugin]
 */
public const val DefaultLogTag: String = "StoreLogging"

/**
 * Log level of this store. Override using [logging] or leave as a default for sensible log levels.
 * Not used when the logger does not support levels, such as with [consoleLoggingPlugin].
 */
public enum class StoreLogLevel {

    Trace, Debug, Info, Warn, Error
}

/**
 * Install a new [loggingPlugin].
 * @param log a function that prints the message using provided [StoreLogLevel] and tag if needed.
 * [pro.respawn.flowmvi.api.Store.name] is used as a tag by default.
 */
@FlowMVIDSL
public fun <S : MVIState, I : MVIIntent, A : MVIAction> StoreBuilder<S, I, A>.logging(
    tag: String? = this.name,
    name: String = "${tag.orEmpty()}Logging",
    log: (level: StoreLogLevel, tag: String, msg: String) -> Unit
): Unit = install(loggingPlugin(tag, name, log))

/**
 * Create a new [StorePlugin] that prints messages using [log].
 * [tag] is used as a name for the plugin.
 * Tag can be null, in which case, [DefaultLogTag] will be used, but the name will remain null.
 */
@FlowMVIDSL
public inline fun <S : MVIState, I : MVIIntent, A : MVIAction> loggingPlugin(
    tag: String?,
    name: String = "${tag.orEmpty()}Logging",
    crossinline log: (level: StoreLogLevel, tag: String, msg: String) -> Unit,
): StorePlugin<S, I, A> = genericPlugin {
    this.name = name
    onState { old, new -> log(Trace, tag ?: name, "\nState:\n--->\n$old\n<---\n$new") }
    onIntent { log(Debug, tag ?: name, "Intent -> $it") }
    onAction { log(Debug, tag ?: name, "Action -> $it") }
    onException { log(Error, tag ?: name, "Exception:\n $it") }
    onStart { log(Info, tag ?: name, "Started") }
    onSubscribe { log(Info, tag ?: name, "New subscriber #${it + 1}") }
    onUnsubscribe { log(Info, tag ?: name, "Subscriber #${it + 1} removed") }
    onStop { log(Info, tag ?: name, "Stopped with e=$it") }
}

/**
 * A logging plugin that prints logs to the console using [println]. Tag is not used except for naming the plugin.
 * For android, use androidLoggingPlugin.
 * @see loggingPlugin
 */
@FlowMVIDSL
public fun <S : MVIState, I : MVIIntent, A : MVIAction> consoleLoggingPlugin(
    tag: String? = null,
    name: String = "${tag.orEmpty()}Logging",
): StorePlugin<S, I, A> = loggingPlugin(tag, name) { _, _, msg -> println("$tag: $msg") }
