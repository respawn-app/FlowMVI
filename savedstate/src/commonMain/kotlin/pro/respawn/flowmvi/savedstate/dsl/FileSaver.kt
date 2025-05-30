package pro.respawn.flowmvi.savedstate.dsl

import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import pro.respawn.flowmvi.savedstate.api.Saver
import pro.respawn.flowmvi.savedstate.platform.read
import pro.respawn.flowmvi.savedstate.platform.readCompressed
import pro.respawn.flowmvi.savedstate.platform.write
import pro.respawn.flowmvi.savedstate.platform.writeCompressed

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
public inline fun <T> DefaultFileSaver(
    path: String,
    crossinline write: suspend (data: T?, toPath: String) -> Unit,
    crossinline read: suspend (fromPath: String) -> T?,
): Saver<T> = object : Saver<T> {

    // prevent concurrent file access
    private val mutex = Mutex()

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
public fun FileSaver(
    path: String,
): Saver<String> = DefaultFileSaver(
    path = path,
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
public fun CompressedFileSaver(
    path: String,
): Saver<String> = DefaultFileSaver(
    path = path,
    write = ::writeCompressed,
    read = ::readCompressed,
)
