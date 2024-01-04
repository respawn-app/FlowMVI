package pro.respawn.flowmvi.savedstate.dsl

import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import pro.respawn.flowmvi.savedstate.api.Saver
import pro.respawn.flowmvi.savedstate.api.ThrowRecover

/**
 * A [Saver] implementation that saves the given state to a file in a specified [dir] and [fileName].
 *
 * * You still need to provide your own [write] and [read] functions for this overload.
 * Use [FileSaver] and [CompressedFileSaver] if you want to save already serialized state.
 *
 * * This saver creates the necessary [dir] and file if not present and writes to file in an atomic way using a [Mutex].
 * * If `null` is passed to [Saver.save], it will delete the file, but not the directory.
 * * The writes to the file cannot be canceled to prevent saving partial data.
 */
public inline fun <T> DefaultFileSaver(
    dir: String,
    fileName: String,
    crossinline write: suspend (data: T, to: Path) -> Unit,
    crossinline read: suspend (from: Path) -> T?,
    crossinline recover: suspend (Exception) -> T?,
): Saver<T> = object : Saver<T> {
    val directory = Path(dir)
    val file = Path(directory, fileName)

    // prevent concurrent file access
    private val mutex = Mutex()

    override suspend fun recover(e: Exception): T? = recover.invoke(e)
    override suspend fun save(
        state: T?
    ) = withContext(NonCancellable) { // prevent partial writes
        mutex.withLock {
            with(SystemFileSystem) {
                if (state == null) {
                    delete(file, false)
                } else {
                    createDirectories(directory)
                    write(state, file)
                }
            }
        }
    }

    // allow cancelling reads (no "NonCancellable here")
    override suspend fun restore(): T? = mutex.withLock {
        file.takeIf { SystemFileSystem.exists(file) }?.let { read(it) }
    }
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
    dir: String,
    fileName: String,
    recover: suspend (Exception) -> String? = ThrowRecover,
): Saver<String> = DefaultFileSaver(
    dir = dir,
    fileName = fileName,
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
public fun CompressedFileSaver(
    dir: String,
    fileName: String,
    recover: suspend (Exception) -> String? = ThrowRecover,
): Saver<String> = DefaultFileSaver(
    dir = dir,
    fileName = fileName,
    recover = recover,
    write = ::writeCompressed,
    read = ::readCompressed,
)
