package pro.respawn.flowmvi.savedstate.dsl

import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import pro.respawn.flowmvi.savedstate.api.Saver
import pro.respawn.flowmvi.savedstate.api.ThrowRecover

public inline fun DefaultFileSaver(
    dir: String,
    fileName: String,
    crossinline write: suspend (data: String, to: Path) -> Unit,
    crossinline read: suspend (from: Path) -> String?,
    crossinline recover: suspend (Exception) -> String?,
): Saver<String> = object : Saver<String> {
    val directory = Path(dir)
    val file = Path(directory, fileName)

    // prevent concurrent file access
    private val mutex = Mutex()

    override suspend fun recover(e: Exception): String? = recover.invoke(e)
    override suspend fun save(
        state: String?
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
    override suspend fun restore(): String? = mutex.withLock {
        file.takeIf { SystemFileSystem.exists(file) }
            ?.let { read(it) }
            ?.takeIf(String::isNotBlank)
    }
}

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
