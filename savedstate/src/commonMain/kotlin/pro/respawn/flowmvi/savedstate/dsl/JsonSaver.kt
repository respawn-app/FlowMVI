package pro.respawn.flowmvi.savedstate.dsl

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import pro.respawn.flowmvi.savedstate.api.Saver

/**
 * A [Saver] implementation that will transform the given state to a JSON string before passing it to the [delegate].
 * It will use the specified [json] instance and [serializer] to transform the state.
 * By default it will recover by trying the [delegate]s' recover first, but if deserialization fails, it will throw.
 */
public fun <T> JsonSaver(
    json: Json,
    serializer: KSerializer<T>,
    delegate: Saver<String>,
): Saver<T> = Saver(
    save = { state -> delegate.save(state?.let { json.encodeToString(serializer, it) }) },
    restore = { delegate.restore()?.let { json.decodeFromString(serializer, it) } }
)
