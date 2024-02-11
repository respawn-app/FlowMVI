package pro.respawn.flowmvi.savedstate.util

import pro.respawn.flowmvi.savedstate.api.Saver

/**
 * Get the name of the class, removing the "State" suffix, if present
 */
public inline fun <reified T> nameByType(): String? = T::class.simpleName?.removeSuffix("State")

@PublishedApi
internal val PluginNameSuffix: String = "SaveStatePlugin"

@PublishedApi
internal const val EmptyBehaviorsMessage: String = """
You wanted to save the state but have not provided any behaviors.
Please supply at least one behavior or remove the plugin as it would do nothing otherwise.
"""

@PublishedApi
internal suspend fun <S> Saver<S>.saveCatching(state: S?): Unit = try {
    save(state)
} catch (expected: Exception) {
    recover(expected)
    Unit
}

@PublishedApi
internal suspend fun <S> Saver<S>.restoreCatching(): S? = try {
    restore()
} catch (expected: Exception) {
    recover(expected)
}
