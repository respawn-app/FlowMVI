@file:MustUseReturnValue

package pro.respawn.flowmvi.savedstate.dsl

import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import pro.respawn.flowmvi.savedstate.api.Saver
import pro.respawn.flowmvi.savedstate.platform.read
import pro.respawn.flowmvi.savedstate.platform.readCompressed
import pro.respawn.flowmvi.savedstate.platform.write
import pro.respawn.flowmvi.savedstate.platform.writeCompressed

internal const val RecoverDeprecationMessage = """Use RecoveringSaver instead. It allows greater flexibility and will
 handle exceptions even when you manually call your saver functions, unlike
 using the `recover` or `onException` callbacks."""

/**
 * A [Saver] implementation that does nothing but invoke
 * the given [onSave], [onRestore], [onException] callbacks when the state changes
 * @see Saver
 */
@Deprecated(RecoverDeprecationMessage)
public inline fun <T> CallbackSaver(
    delegate: Saver<T>,
    crossinline onSave: suspend (T?) -> Unit = {},
    crossinline onRestore: suspend (T?) -> Unit = {},
    crossinline onException: suspend (e: Exception) -> Unit,
): Saver<T> = object : Saver<T> by delegate {
    override suspend fun save(state: T?) {
        onSave(state)
        return delegate.save(state)
    }

    override suspend fun restore(): T? = delegate.restore().also { onRestore(it) }

    @Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
    override suspend fun recover(e: Exception): T? {
        onException(e)
        return delegate.recover(e)
    }
}

/**
 * A [Saver] implementation that saves the given state to a file in a specified [path]
 *
 * * You still need to provide your own [write] and [read] functions for this overload.
 * Use [FileSaver] and [CompressedFileSaver] if you want to save already serialized state.
 *
 * * This saver creates the necessary directories and files if not present and
 * writes to file in an atomic way using a [Mutex].
 * * If `null` is passed to [Saver.save], it will delete the file, but not the directory.
 * * The writes to the file cannot be canceled to prevent saving partial data.
 */
@Deprecated(RecoverDeprecationMessage)
public fun <T> DefaultFileSaver(
    path: String,
    write: suspend (data: T?, toPath: String) -> Unit,
    read: suspend (fromPath: String) -> T?,
    recover: suspend (Exception) -> T?,
): Saver<T> = object : Saver<T> {

    // prevent concurrent file access
    private val mutex = Mutex()

    @Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
    override suspend fun recover(e: Exception): T? = recover.invoke(e)

    // prevent partial writes
    override suspend fun save(state: T?) = withContext(NonCancellable) {
        mutex.withLock { write(state, path) }
    }

    // allow cancelling reads (no "NonCancellable" here)
    override suspend fun restore(): T? = mutex.withLock { read(path) }
}

/**
 * A [DefaultFileSaver] implementation that saves [String] state to the file system.
 *
 * Usually used as a decorator for [JsonSaver]
 *
 * See the overload for more details.
 * @see DefaultFileSaver
 * @see JsonSaver
 * @see Saver
 */
@Deprecated(RecoverDeprecationMessage)
@Suppress("DEPRECATION")
public fun FileSaver(
    path: String,
    recover: suspend (Exception) -> String?,
): Saver<String> = DefaultFileSaver(
    path = path,
    recover = recover,
    write = ::write,
    read = ::read,
)

/**
 * A [DefaultFileSaver] implementation that saves a **compressed** [String] state to the file system.
 * Usually used as a decorator for [JsonSaver]
 *
 * This saver is only available on JVM and Android for now, and therefore will be identical to [FileSaver]
 * on other platforms. (will **not** compress the file)
 *
 * See the overload for more details.
 *
 * @see DefaultFileSaver
 * @see JsonSaver
 * @see Saver
 */
@Deprecated(RecoverDeprecationMessage)
@Suppress("DEPRECATION")
public fun CompressedFileSaver(
    path: String,
    recover: suspend (Exception) -> String?,
): Saver<String> = DefaultFileSaver(
    path = path,
    recover = recover,
    write = ::writeCompressed,
    read = ::readCompressed,
)

/**
 * A [Saver] implementation that will transform the given state to a JSON string before passing it to the [delegate].
 * It will use the specified [json] instance and [serializer] to transform the state.
 * By default it will recover by trying the [delegate]s' recover first, but if deserialization fails, it will throw.
 */
public fun <T> JsonSaver(
    json: Json,
    serializer: KSerializer<T>,
    delegate: Saver<String>,
    recover: suspend (Exception) -> T?,
): Saver<T> = Saver(
    recover = recover,
    save = { state -> delegate.save(state?.let { json.encodeToString(serializer, it) }) },
    restore = { delegate.restore()?.let { json.decodeFromString(serializer, it) } }
)

/**
 * A [Saver] builder function
 */
public inline fun <T> Saver(
    crossinline save: suspend (T?) -> Unit,
    crossinline restore: suspend () -> T?,
    crossinline recover: suspend (e: Exception) -> T?,
): Saver<T> = object : Saver<T> {
    override suspend fun save(state: T?) = save.invoke(state)
    override suspend fun restore(): T? = restore.invoke()

    @Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
    override suspend fun recover(e: Exception): T? = recover.invoke(e)
}
