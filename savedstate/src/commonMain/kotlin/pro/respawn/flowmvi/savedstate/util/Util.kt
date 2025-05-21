package pro.respawn.flowmvi.savedstate.util

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import pro.respawn.flowmvi.api.MVIState

@PublishedApi
internal val PluginNameSuffix: String = "SaveStatePlugin"

@PublishedApi
internal const val EmptyBehaviorsMessage: String = """
You wanted to save the state but have not provided any behaviors.
Please supply at least one behavior or remove the plugin as it would do nothing otherwise.
"""

@PublishedApi
@OptIn(ExperimentalSerializationApi::class)
internal val DefaultJson: Json = Json {
    decodeEnumsCaseInsensitive = true
    explicitNulls = false
    coerceInputValues = true
    allowTrailingComma = true
    useAlternativeNames = true
}

/**
 * Get the name of the class, removing the "State" suffix, if present.
 */
@Deprecated("Usage of this function leads to some unintended consequences when enabling code obfuscation")
public inline fun <reified T : MVIState> nameByType(): String? = T::class.simpleName?.removeSuffix("State")
