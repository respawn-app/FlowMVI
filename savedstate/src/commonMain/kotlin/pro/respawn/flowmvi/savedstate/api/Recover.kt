package pro.respawn.flowmvi.savedstate.api

/**
 * A [Saver] recovery function that will simply return `null` value and no state will be saved/restored.
 * Existing state will be cleared.
 */
public val NullRecover: suspend (Exception) -> Nothing? = { null }

/**
 * A [Saver] recovery function that will throw on any exception when saving and restoring state.
 *
 * Usually, this is the default.
 */
public val ThrowRecover: suspend (e: Exception) -> Nothing? = { throw it }
