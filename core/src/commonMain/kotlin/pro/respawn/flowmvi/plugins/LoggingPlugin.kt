@file:Suppress("StringShouldBeRawString")

package pro.respawn.flowmvi.plugins

import pro.respawn.flowmvi.api.FlowMVIDSL
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.StorePlugin
import pro.respawn.flowmvi.dsl.StoreBuilder
import pro.respawn.flowmvi.dsl.plugin
import pro.respawn.flowmvi.logging.PlatformStoreLogger
import pro.respawn.flowmvi.logging.StoreLogLevel
import pro.respawn.flowmvi.logging.StoreLogLevel.Debug
import pro.respawn.flowmvi.logging.StoreLogLevel.Error
import pro.respawn.flowmvi.logging.StoreLogLevel.Info
import pro.respawn.flowmvi.logging.StoreLogLevel.Trace
import pro.respawn.flowmvi.logging.StoreLogger
import pro.respawn.flowmvi.logging.invoke
import kotlin.math.log

/**
 * Install a new [loggingPlugin].
 * @param logger a [StoreLogger] that prints the message using provided [StoreLogLevel] and tag if needed.
 * [pro.respawn.flowmvi.api.Store.name] is used as a tag by default.
 */
@FlowMVIDSL
public fun <S : MVIState, I : MVIIntent, A : MVIAction> StoreBuilder<S, I, A>.logging(
    logger: StoreLogger? = this.logger,
    tag: String? = this.name,
    name: String = "${tag}Logging",
    level: StoreLogLevel? = null,
) {
    install(loggingPlugin(logger ?: return, tag, name, level))
}

/**
 * Create a new [StorePlugin] that prints messages using [log].
 * [tag] is used as a name for the plugin.
 * Tag can be null, in which case, [name] will be used, will remain null.
 * [level] level override to print all messages. If null, a default level will be used (null by default)
 */
@Suppress("CyclomaticComplexMethod") // false-positive based on ternary ops
@FlowMVIDSL
public fun <S : MVIState, I : MVIIntent, A : MVIAction> loggingPlugin(
    logger: StoreLogger? = PlatformStoreLogger,
    tag: String? = null,
    name: String = "${tag}Logging",
    level: StoreLogLevel? = null,
): StorePlugin<S, I, A> = plugin {
    this.name = name
    onState { old, new ->
        logger(level ?: Trace, tag) { "\nState:\n--->\n$old\n<---\n$new" }
        new
    }
    onIntent {
        logger(level ?: Debug, tag) { "Intent -> $it" }
        it
    }
    onAction {
        logger(level ?: Debug, tag) { "Action -> $it" }
        it
    }
    onException {
        logger(level ?: Error, tag, it)
        it
    }
    onStart { logger(level ?: Info, tag) { "Started $tag" } }
    onSubscribe { subs -> logger(level ?: Info, tag) { "New subscriber #$subs" } }
    onUnsubscribe { logger(level ?: Info, tag) { "Subscriber #${it + 1} removed" } }
    onStop {
        if (it == null) {
            logger(level ?: Info, tag) { "Stopped $tag" }
            return@onStop
        }
        logger(level ?: Error, tag) { "Stopped with exception: " }
        logger(level ?: Error, tag, it)
    }
}
