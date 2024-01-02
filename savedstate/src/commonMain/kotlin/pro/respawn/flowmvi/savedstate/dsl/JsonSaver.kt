package pro.respawn.flowmvi.savedstate.dsl

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import pro.respawn.flowmvi.savedstate.api.Saver

public inline fun <T> JsonSaver(
    json: Json,
    delegate: Saver<String>,
    serializer: KSerializer<T>,
    noinline recover: suspend (Exception) -> T? = { e ->
        delegate.recover(e)?.let { json.decodeFromString(serializer, it) }
    },
): Saver<T> = object : Saver<T> {

    override suspend fun recover(e: Exception): T? = recover.invoke(e)

    override suspend fun save(state: T?) {
        delegate.save(state?.let { json.encodeToString(serializer, it) })
    }

    override suspend fun restore(): T? = delegate.restore()?.let { json.decodeFromString(serializer, it) }
}
