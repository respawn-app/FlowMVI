package pro.respawn.flowmvi.savedstate.dsl

import pro.respawn.flowmvi.logging.PlatformStoreLogger
import pro.respawn.flowmvi.logging.StoreLogLevel
import pro.respawn.flowmvi.logging.StoreLogger
import pro.respawn.flowmvi.logging.invoke
import pro.respawn.flowmvi.savedstate.api.Saver

/**
 * A [Saver] that writes to [logger] during save restoration, saving and errors.
 */
public fun <T> LoggingSaver(
    delegate: Saver<T>,
    logger: StoreLogger = PlatformStoreLogger,
    level: StoreLogLevel? = null,
    tag: String? = "Saver",
): Saver<T> = CallbackSaver(
    delegate,
    onSave = { logger(level ?: StoreLogLevel.Trace, tag) { "Saving state: $it" } },
    onRestore = { logger(level ?: StoreLogLevel.Trace, tag) { "Restored state: $it" } },
    onException = { logger.invoke(level ?: StoreLogLevel.Error, tag, e = it) },
)
