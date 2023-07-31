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
    tag: String? = name,
    log: (level: StoreLogLevel, tag: String, msg: String) -> Unit
): Unit = install(loggingPlugin(tag, log))

/**
 * Create a new [StorePlugin] that prints messages using [log].
 * [tag] is used as a name for the plugin.
 * Tag can be null, in which case, [DefaultLogTag] will be used, but the name will remain null.
 */
@FlowMVIDSL
public inline fun <S : MVIState, I : MVIIntent, A : MVIAction> loggingPlugin(
    tag: String? = null,
    crossinline log: (level: StoreLogLevel, tag: String, msg: String) -> Unit,
): StorePlugin<S, I, A> = genericPlugin {
    val realTag = tag ?: DefaultLogTag
    name = tag
    onState { old, new -> log(Trace, realTag, "\nState:\n--->\n$old\n<---\n$new") }
    onIntent { log(Debug, realTag, "Intent -> $it") }
    onAction { log(Debug, realTag, "Action -> $it") }
    onException { log(Error, realTag, "Exception:\n ${it.stackTraceToString()}") }
    onStart { log(Info, realTag, "Started") }
    onSubscribe { log(Info, realTag, "New subscriber #${it + 1}") }
    onStop { log(Info, realTag, "Stopped") }
}

/**
 * A logging plugin that prints logs to the console using [println]. Tag is not used except for naming the plugin.
 * For android, use androidLoggingPlugin.
 * @see loggingPlugin
 */
@FlowMVIDSL
public fun <S : MVIState, I : MVIIntent, A : MVIAction> consoleLoggingPlugin(
    tag: String? = null
): StorePlugin<S, I, A> = loggingPlugin(tag) { _, _, msg -> println("$tag: $msg") }
