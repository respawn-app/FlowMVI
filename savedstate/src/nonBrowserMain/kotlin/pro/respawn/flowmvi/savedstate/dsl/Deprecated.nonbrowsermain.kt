package pro.respawn.flowmvi.savedstate.dsl

import kotlinx.io.files.Path
import pro.respawn.flowmvi.savedstate.api.Saver
import pro.respawn.flowmvi.savedstate.api.ThrowRecover
import pro.respawn.flowmvi.savedstate.platform.read
import pro.respawn.flowmvi.savedstate.platform.readCompressed
import pro.respawn.flowmvi.savedstate.platform.write
import pro.respawn.flowmvi.savedstate.platform.writeCompressed

/**
 * A [Saver] implementation that saves the given state to a file in a specified [dir] and [fileName].
 *
 * * You still need to provide your own [write] and [read] functions for this overload.
 * Use [FileSaver] and [CompressedFileSaver] if you want to save already serialized state.
 *
 * * This saver creates the necessary [dir] and file if not present and writes to file in an atomic way using a mutex.
 * * If `null` is passed to [Saver.save], it will delete the file, but not the directory.
 * * The writes to the file cannot be canceled to prevent saving partial data.
 */
@Deprecated("Please use an overload that uses a path instead")
@Suppress("DEPRECATION")
public inline fun <T> DefaultFileSaver(
    dir: String,
    fileName: String,
    crossinline write: suspend (data: T?, to: Path) -> Unit,
    crossinline read: suspend (from: Path) -> T?,
    noinline recover: suspend (Exception) -> T?,
): Saver<T> = DefaultFileSaver(
    path = Path(dir, fileName).name,
    write = { data: T?, path: String -> write(data, Path(path)) },
    read = { path -> read(Path(path)) },
    recover = recover,
)

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
@Deprecated("Please use an overload that uses a path instead")
@Suppress("DEPRECATION")
public fun FileSaver(
    dir: String,
    fileName: String,
    recover: suspend (Exception) -> String? = ThrowRecover,
): Saver<String> = DefaultFileSaver(
    dir = dir,
    fileName = fileName,
    recover = recover,
    write = { data, path -> write(data, path.name) },
    read = { path -> read(path.name) },
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
@Deprecated("Please use an overload that uses a path instead")
@Suppress("DEPRECATION")
public fun CompressedFileSaver(
    dir: String,
    fileName: String,
    recover: suspend (Exception) -> String? = ThrowRecover,
): Saver<String> = DefaultFileSaver(
    dir = dir,
    fileName = fileName,
    recover = recover,
    write = { data, path -> writeCompressed(data, path.name) },
    read = { path -> readCompressed(path.name) },
)
