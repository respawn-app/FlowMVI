package pro.respawn.flowmvi.sample.util

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import pro.respawn.flowmvi.BuildFlags

@OptIn(ExperimentalSerializationApi::class)
val Json = Json {
    prettyPrint = BuildFlags.debuggable
    decodeEnumsCaseInsensitive = true
    explicitNulls = false
    coerceInputValues = true
    allowTrailingComma = true
    useAlternativeNames = true
}
