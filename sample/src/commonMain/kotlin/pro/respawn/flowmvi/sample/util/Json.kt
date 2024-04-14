package pro.respawn.flowmvi.sample.util

import kotlinx.serialization.json.Json

val Json = Json {
    prettyPrint = BuildFlags.debuggable
    decodeEnumsCaseInsensitive = true
    explicitNulls = false
    coerceInputValues = true
    allowTrailingComma = true
    useAlternativeNames = true
}
