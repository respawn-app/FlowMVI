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
    name: String? = null,
    logger: StoreLogger? = null,
): Unit = loggingPlugin<S, I, A>(tag, level, name, logger).let(::install)

/**
 * Create a new [StorePlugin] that prints messages using [log].
 * * [tag] is used as a name for the plugin, unless overridden by [name]. Tag can be null, in which case, store's name will be used. Provide an empty string to remove the tag.
 * * [level] level override to print all messages. If null, a default level will be used (null by default)
 * * [logger] Unless a non-null value is provided, a store logger will be used.
 */
@Suppress("CyclomaticComplexMethod") // false-positive based on ternary ops
@FlowMVIDSL
public fun <S : MVIState, I : MVIIntent, A : MVIAction> loggingPlugin(
    tag: String? = null,
    level: StoreLogLevel? = null,
    name: String? = null,
    logger: StoreLogger? = null,
): LazyPlugin<S, I, A> = lazyPlugin {
    val currentTag = tag ?: config.name
    this.name = name ?: currentTag.let { "${it.orEmpty()}Logging" }
    val log = logger ?: config.logger
    onState { old, new ->
        if (old == new) return@onState new
        new.also { log(level ?: Trace, currentTag) { "State:\n--->\n$old\n<---\n$new" } }
    }
    onIntent {
        it.also { log(level ?: Debug, currentTag) { "Intent -> $it" } }
    }
    onAction {
        it.also { log(level ?: Debug, currentTag) { "Action -> $it" } }
    }
    onException {
        it.also { log(it, level ?: Error, currentTag) }
    }
    onStart {
        log(level ?: Info, currentTag) { "Started ${config.name ?: "Store"}" }
    }
    onSubscribe {
        log(level ?: Info, currentTag) { "New subscriber #${it + 1}" }
    }
    onUnsubscribe {
        log(level ?: Info, currentTag) { "Subscriber #${it + 1} removed" }
    }
    onStop { e ->
        if (e == null) {
            log(level ?: Info, currentTag) { "Stopped ${config.name ?: "Store"}" }
            return@onStop
        }
        log(level ?: Error, currentTag) { "Stopped with exception: " }
        log(e, level ?: Error, currentTag)
    }
}
