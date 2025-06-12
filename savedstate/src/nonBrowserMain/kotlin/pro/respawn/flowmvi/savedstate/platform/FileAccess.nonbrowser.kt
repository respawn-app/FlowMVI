package pro.respawn.flowmvi.savedstate.platform

import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readString
import kotlinx.io.writeString

@OptIn(ExperimentalStdlibApi::class)
internal actual suspend fun write(data: String?, path: String) {
    SystemFileSystem.run {
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
internal actual suspend fun read(path: String): String? = SystemFileSystem.run {
    val file = Path(path)
    if (!exists(file)) return@run null
    source(file)
        .buffered()
        .use { it.readString() }
        .takeIf { it.isNotBlank() }
}
