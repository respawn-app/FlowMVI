package pro.respawn.flowmvi.savedstate.dsl

import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import pro.respawn.flowmvi.savedstate.api.Saver
import pro.respawn.flowmvi.savedstate.api.ThrowRecover

internal inline fun NonNullFileSaver(
    dir: String,
    fileName: String,
    crossinline write: suspend (data: String, to: Path) -> Unit,
    crossinline read: suspend (from: Path) -> String?,
    noinline recover: suspend (Exception) -> String?,
): Saver<String> = object : Saver<String> {
    val directory = Path(dir)
    val file = Path(directory, fileName)

    override suspend fun recover(e: Exception): String? = recover.invoke(e)
    override suspend fun save(
        state: String?
    ) = with(SystemFileSystem) {
        if (state == null) {
            delete(file, false)
        } else {
            createDirectories(directory)
            write(state, file)
        }
    }

    override suspend fun restore(): String? = file
        .takeIf { SystemFileSystem.exists(file) }
        ?.let { read(it) }
        ?.takeIf { it.isNotBlank() }
}

public fun FileSaver(
    dir: String,
    fileName: String,
    recover: suspend (Exception) -> String? = ThrowRecover,
): Saver<String> = NonNullFileSaver(
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
): Saver<String> = NonNullFileSaver(
    dir = dir,
    fileName = fileName,
    recover = recover,
    write = ::writeCompressed,
    read = ::readCompressed,
)
