@file:Suppress("StringShouldBeRawString")

package pro.respawn.flowmvi.plugins

import pro.respawn.flowmvi.api.FlowMVIDSL
import pro.respawn.flowmvi.api.LazyPlugin
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.StorePlugin
import pro.respawn.flowmvi.dsl.StoreBuilder
import pro.respawn.flowmvi.dsl.lazyPlugin
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
 * @param level [StoreLogLevel] to override the defaults
 * @param tag tag to use, or the default tag will be used
 * [pro.respawn.flowmvi.api.Store.name] is used as a tag by default.
 */
@FlowMVIDSL
public fun <S : MVIState, I : MVIIntent, A : MVIAction> StoreBuilder<S, I, A>.enableLogging(
    tag: String? = null,
    level: StoreLogLevel? = null,
): Unit = loggingPlugin<S, I, A>(tag, level).let(::install)

/**
 * Create a new [StorePlugin] that prints messages using [log].
 * [tag] is used as a name for the plugin.
 * Tag can be null, in which case, [name] will be used, will remain null.
 * [level] level override to print all messages. If null, a default level will be used (null by default)
 */
@Suppress("CyclomaticComplexMethod") // false-positive based on ternary ops
@FlowMVIDSL
public fun <S : MVIState, I : MVIIntent, A : MVIAction> loggingPlugin(
    tag: String? = null,
    level: StoreLogLevel? = null,
): LazyPlugin<S, I, A> = lazyPlugin {
    val currentTag = tag ?: config.name
    this.name = currentTag.let { "${it.orEmpty()}Logging" }
    val logger = config.logger
    onState { old, new ->
        logger(level ?: Trace, currentTag) { "State:\n--->\n$old\n<---\n$new" }
        new
    }
    onIntent {
        logger(level ?: Debug, currentTag) { "Intent -> $it" }
        it
    }
    onAction {
        logger(level ?: Debug, currentTag) { "Action -> $it" }
        it
    }
    onException {
        logger(it, level ?: Error, currentTag)
        it
    }
    onStart { logger(level ?: Info, currentTag) { "Started ${config.name ?: "Store"}" } }
    onSubscribe { logger(level ?: Info, currentTag) { "New subscriber #${it + 1}" } }
    onUnsubscribe { logger(level ?: Info, currentTag) { "Subscriber #${it + 1} removed" } }
    onStop {
        if (it == null) {
            logger(level ?: Info, currentTag) { "Stopped ${config.name ?: "Store"}" }
            return@onStop
        }
        logger(level ?: Error, currentTag) { "Stopped with exception: " }
        logger(it, level ?: Error, currentTag)
    }
}
