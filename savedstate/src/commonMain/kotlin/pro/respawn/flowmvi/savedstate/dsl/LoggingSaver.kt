@file:pro.respawn.flowmvi.annotation.MustUseReturnValues

package pro.respawn.flowmvi.savedstate.dsl

import pro.respawn.flowmvi.logging.StoreLogLevel
import pro.respawn.flowmvi.logging.StoreLogger
import pro.respawn.flowmvi.logging.invoke
import pro.respawn.flowmvi.savedstate.api.Saver

/**
 * A [Saver] that writes to [logger] during save restoration, saving and errors.
 */
@Suppress("DEPRECATION") // recover
public fun <T> LoggingSaver(
    delegate: Saver<T>,
    logger: StoreLogger,
    level: StoreLogLevel? = null,
    tag: String? = "Saver",
): Saver<T> = CallbackSaver(
    delegate,
    onSave = { logger(level ?: StoreLogLevel.Trace, tag) { "Saving state: $it" } },
    onRestore = { logger(level ?: StoreLogLevel.Trace, tag) { "Restored state: $it" } },
    onException = { logger(it, level ?: StoreLogLevel.Error, tag) },
)
