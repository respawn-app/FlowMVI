package pro.respawn.flowmvi.savedstate.platform

import kotlinx.io.buffered
import kotlinx.io.files.FileSystem
import kotlinx.io.files.Path
import kotlinx.io.readString
import kotlinx.io.writeString

internal expect suspend fun writeCompressed(data: String?, path: String)

internal expect suspend fun readCompressed(path: String): String?

internal expect suspend fun write(data: String?, path: String)

internal expect suspend fun read(path: String): String?

@OptIn(ExperimentalStdlibApi::class)
internal fun write(data: String?, path: String, fileSystem: FileSystem) {
    fileSystem.run {
        val file = Path(path)
        if (data == null) {
            delete(file, false)
            return@run
        }
        file.parent?.let { createDirectories(it) }
        sink(file).buffered().use { it.writeString(data) }
    }
}

@OptIn(ExperimentalStdlibApi::class)
internal fun read(path: String, fileSystem: FileSystem): String? = fileSystem.run {
    val file = Path(path)
    if (!exists(file)) return@run null
    source(file)
        .buffered()
        .use { it.readString() }
        .takeIf { it.isNotBlank() }
}
